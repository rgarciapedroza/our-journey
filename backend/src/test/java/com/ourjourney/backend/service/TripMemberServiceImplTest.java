package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.ourjourney.backend.dto.UserSearchResponse;
import com.ourjourney.backend.entity.Trip;
import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.entity.TripMemberRole;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.TripMemberRepository;
import com.ourjourney.backend.repository.TripRepository;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.impl.TripMemberServiceImpl;

@ExtendWith(MockitoExtension.class)
class TripMemberServiceImplTest {

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripMemberServiceImpl tripMemberService;

    @Test
    void shouldReturnEmptyResultsWhenSearchQueryIsTooShort() {
        List<UserSearchResponse> results = tripMemberService.searchAvailableUsers(
                1L,
                "a",
                "owner@example.com"
        );

        assertEquals(List.of(), results);
        verifyNoInteractions(userRepository, tripMemberRepository, tripRepository);
    }

    @Test
    void shouldReturnAvailableUsersWithProfileDetails() {
        Long tripId = 10L;
        User owner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .build();
        TripMember ownerMembership = TripMember.builder()
                .trip(Trip.builder().id(tripId).build())
                .user(owner)
                .role(TripMemberRole.OWNER)
                .build();
        User candidate = User.builder()
                .id(2L)
                .name("Rosmary Smith")
                .email("rosmary@example.com")
                .profilePicture("https://example.com/profile.jpg")
                .build();

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(tripMemberRepository.findByTripIdAndUserId(tripId, owner.getId()))
                .thenReturn(Optional.of(ownerMembership));
        when(userRepository.searchAvailableTripMembers(
                org.mockito.ArgumentMatchers.eq(tripId),
                org.mockito.ArgumentMatchers.eq("ros"),
                any(Pageable.class)
        )).thenReturn(List.of(candidate));

        List<UserSearchResponse> results = tripMemberService.searchAvailableUsers(
                tripId,
                "  ros  ",
                owner.getEmail()
        );

        assertEquals(1, results.size());
        assertEquals(candidate.getId(), results.getFirst().id());
        assertEquals(candidate.getName(), results.getFirst().name());
        assertEquals(candidate.getEmail(), results.getFirst().email());
        assertEquals(candidate.getProfilePicture(), results.getFirst().profilePicture());

        verify(userRepository).searchAvailableTripMembers(
                org.mockito.ArgumentMatchers.eq(tripId),
                org.mockito.ArgumentMatchers.eq("ros"),
                org.mockito.ArgumentMatchers.argThat(pageable ->
                        pageable.getPageNumber() == 0 && pageable.getPageSize() == 10
                )
        );
    }

    @Test
    void shouldRejectSearchWhenCurrentUserIsNotTheOwner() {
        Long tripId = 10L;
        User member = User.builder()
                .id(2L)
                .email("member@example.com")
                .build();
        TripMember membership = TripMember.builder()
                .user(member)
                .role(TripMemberRole.MEMBER)
                .build();

        when(userRepository.findByEmail(member.getEmail()))
                .thenReturn(Optional.of(member));
        when(tripMemberRepository.findByTripIdAndUserId(tripId, member.getId()))
                .thenReturn(Optional.of(membership));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripMemberService.searchAvailableUsers(
                        tripId,
                        "ros",
                        member.getEmail()
                )
        );

        verify(userRepository, never()).searchAvailableTripMembers(
                any(),
                any(),
                any(Pageable.class)
        );
    }
}
