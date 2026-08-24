package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ourjourney.backend.dto.ItineraryItemRequest;
import com.ourjourney.backend.dto.ItineraryItemResponse;
import com.ourjourney.backend.entity.ItineraryItem;
import com.ourjourney.backend.entity.Trip;
import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.entity.TripMemberRole;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.exception.BadRequestException;
import com.ourjourney.backend.exception.ResourceNotFoundException;
import com.ourjourney.backend.repository.ItineraryItemRepository;
import com.ourjourney.backend.service.impl.ItineraryItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class ItineraryItemServiceImplTest {

    private static final Long TRIP_ID = 1L;
    private static final Long ITEM_ID = 10L;
    private static final String USER_EMAIL = "member@example.com";
    private static final LocalDate TRIP_START = LocalDate.of(2026, 9, 10);
    private static final LocalDate TRIP_END = LocalDate.of(2026, 9, 20);
    private static final Instant CREATED_AT = Instant.parse("2026-08-24T09:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-08-24T09:00:00Z");

    @Mock
    private ItineraryItemRepository itineraryItemRepository;

    @Mock
    private TripAuthorizationService tripAuthorizationService;

    @InjectMocks
    private ItineraryItemServiceImpl itineraryItemService;

    private Trip trip;
    private User user;
    private TripMember membership;

    @BeforeEach
    void setUp() {
        trip = Trip.builder()
                .id(TRIP_ID)
                .name("Rome")
                .startDate(TRIP_START)
                .endDate(TRIP_END)
                .build();
        user = User.builder()
                .id(2L)
                .name("Rosmary")
                .email(USER_EMAIL)
                .profilePicture("https://example.com/profile.jpg")
                .build();
        membership = TripMember.builder()
                .id(3L)
                .trip(trip)
                .user(user)
                .role(TripMemberRole.MEMBER)
                .build();
    }

    @Test
    void shouldReturnChronologicalItemsForTripMember() {
        ItineraryItem item = savedItem(
                user,
                TRIP_START,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                "Visit the Colosseum",
                "Guided visit",
                "Colosseum"
        );
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);
        when(itineraryItemRepository
                .findByTripIdOrderByActivityDateAscStartTimeAsc(TRIP_ID))
                .thenReturn(List.of(item));

        List<ItineraryItemResponse> responses =
                itineraryItemService.getItems(TRIP_ID, USER_EMAIL);

        assertEquals(1, responses.size());
        ItineraryItemResponse response = responses.get(0);
        assertEquals(ITEM_ID, response.getId());
        assertEquals(TRIP_ID, response.getTripId());
        assertEquals(user.getId(), response.getCreatedById());
        assertEquals("Visit the Colosseum", response.getTitle());
        assertEquals(CREATED_AT, response.getCreatedAt());
    }

    @Test
    void shouldNotLoadItemsWhenTripAuthorizationFails() {
        IllegalArgumentException authorizationError =
                new IllegalArgumentException("You are not a member of this trip");
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenThrow(authorizationError);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> itineraryItemService.getItems(TRIP_ID, USER_EMAIL)
        );

        assertSame(authorizationError, thrown);
        verify(itineraryItemRepository, never())
                .findByTripIdOrderByActivityDateAscStartTimeAsc(any());
    }

    @Test
    void shouldCreateItemAndNormalizeOptionalText() {
        ItineraryItemRequest request = validRequest();
        request.setActivityDate(TRIP_START);
        request.setTitle("  Visit the Colosseum  ");
        request.setDescription("   ");
        request.setPlace("  Piazza del Colosseo  ");
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);
        when(itineraryItemRepository.save(any(ItineraryItem.class)))
                .thenAnswer(invocation -> savedCopy(invocation.getArgument(0)));

        ItineraryItemResponse response = itineraryItemService.createItem(
                TRIP_ID,
                request,
                USER_EMAIL
        );

        ArgumentCaptor<ItineraryItem> captor =
                ArgumentCaptor.forClass(ItineraryItem.class);
        verify(itineraryItemRepository).save(captor.capture());
        ItineraryItem item = captor.getValue();
        assertSame(trip, item.getTrip());
        assertSame(user, item.getCreatedBy());
        assertEquals("Visit the Colosseum", item.getTitle());
        assertNull(item.getDescription());
        assertEquals("Piazza del Colosseo", item.getPlace());
        assertEquals(ITEM_ID, response.getId());
    }

    @Test
    void shouldRejectItemWhenEndTimeIsNotAfterStartTime() {
        ItineraryItemRequest request = validRequest();
        request.setEndTime(request.getStartTime());
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> itineraryItemService.createItem(TRIP_ID, request, USER_EMAIL)
        );

        assertEquals("End time must be after start time", exception.getMessage());
        verify(itineraryItemRepository, never()).save(any());
    }

    @Test
    void shouldRejectItemOutsideTripDates() {
        ItineraryItemRequest request = validRequest();
        request.setActivityDate(TRIP_END.plusDays(1));
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> itineraryItemService.createItem(TRIP_ID, request, USER_EMAIL)
        );

        assertEquals(
                "Activity date must be within the trip dates",
                exception.getMessage()
        );
        verify(itineraryItemRepository, never()).save(any());
    }

    @Test
    void shouldAllowAnyTripMemberToUpdateItem() {
        User creator = User.builder()
                .id(99L)
                .name("Another member")
                .email("another@example.com")
                .build();
        ItineraryItem item = savedItem(
                creator,
                TRIP_START,
                LocalTime.of(9, 0),
                null,
                "Old title",
                null,
                null
        );
        ItineraryItemRequest request = validRequest();
        request.setTitle("Updated activity");
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);
        when(itineraryItemRepository.findByIdAndTripId(ITEM_ID, TRIP_ID))
                .thenReturn(Optional.of(item));

        ItineraryItemResponse response = itineraryItemService.updateItem(
                TRIP_ID,
                ITEM_ID,
                request,
                USER_EMAIL
        );

        assertEquals("Updated activity", item.getTitle());
        assertEquals(request.getActivityDate(), item.getActivityDate());
        assertEquals("Updated activity", response.getTitle());
        verify(itineraryItemRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenItemDoesNotBelongToTrip() {
        ItineraryItemRequest request = validRequest();
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);
        when(itineraryItemRepository.findByIdAndTripId(ITEM_ID, TRIP_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> itineraryItemService.updateItem(
                        TRIP_ID,
                        ITEM_ID,
                        request,
                        USER_EMAIL
                )
        );

        assertEquals("Itinerary item not found", exception.getMessage());
    }

    @Test
    void shouldAllowAnyTripMemberToDeleteItem() {
        ItineraryItem item = savedItem(
                user,
                TRIP_START,
                LocalTime.of(10, 0),
                null,
                "Lunch",
                null,
                "Trastevere"
        );
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);
        when(itineraryItemRepository.findByIdAndTripId(ITEM_ID, TRIP_ID))
                .thenReturn(Optional.of(item));

        itineraryItemService.deleteItem(TRIP_ID, ITEM_ID, USER_EMAIL);

        verify(itineraryItemRepository).delete(item);
    }

    @Test
    void shouldRejectDeletionWhenItemDoesNotBelongToTrip() {
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);
        when(itineraryItemRepository.findByIdAndTripId(ITEM_ID, TRIP_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> itineraryItemService.deleteItem(TRIP_ID, ITEM_ID, USER_EMAIL)
        );

        assertEquals("Itinerary item not found", exception.getMessage());
        verify(itineraryItemRepository, never()).delete(any());
    }

    private ItineraryItemRequest validRequest() {
        ItineraryItemRequest request = new ItineraryItemRequest();
        request.setActivityDate(LocalDate.of(2026, 9, 12));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(12, 0));
        request.setTitle("Visit the Colosseum");
        request.setDescription("Guided visit");
        request.setPlace("Colosseum");
        return request;
    }

    private ItineraryItem savedItem(
            User creator,
            LocalDate activityDate,
            LocalTime startTime,
            LocalTime endTime,
            String title,
            String description,
            String place
    ) {
        return ItineraryItem.builder()
                .id(ITEM_ID)
                .trip(trip)
                .createdBy(creator)
                .activityDate(activityDate)
                .startTime(startTime)
                .endTime(endTime)
                .title(title)
                .description(description)
                .place(place)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    private ItineraryItem savedCopy(ItineraryItem item) {
        return ItineraryItem.builder()
                .id(ITEM_ID)
                .trip(item.getTrip())
                .createdBy(item.getCreatedBy())
                .activityDate(item.getActivityDate())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .title(item.getTitle())
                .description(item.getDescription())
                .place(item.getPlace())
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }
}
