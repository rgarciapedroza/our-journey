package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ourjourney.backend.entity.Trip;
import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.entity.TripMemberRole;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.TripMemberRepository;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.impl.TripAuthorizationServiceImpl;

@ExtendWith(MockitoExtension.class)
class TripAuthorizationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @InjectMocks
    private TripAuthorizationServiceImpl authorizationService;

    private Trip trip;
    private User owner;
    private User member;
    private TripMember ownerMembership;
    private TripMember memberMembership;

    @BeforeEach
    void setUp() {

        trip = Trip.builder()
                .id(1L)
                .name("The Canary Islands 2027")
                .build();

        owner = User.builder()
                .id(1L)
                .name("Rosmary")
                .email("owner@example.com")
                .build();

        member = User.builder()
                .id(2L)
                .name("Member")
                .email("member@example.com")
                .build();

        ownerMembership = TripMember.builder()
                .id(1L)
                .trip(trip)
                .user(owner)
                .role(TripMemberRole.OWNER)
                .build();

        memberMembership = TripMember.builder()
                .id(2L)
                .trip(trip)
                .user(member)
                .role(TripMemberRole.MEMBER)
                .build();
    }

    @Test
    void shouldReturnCurrentMember() {

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                owner.getId()
        )).thenReturn(Optional.of(ownerMembership));

        TripMember result =
                authorizationService.getCurrentMember(
                        1L,
                        "owner@example.com"
                );

        assertSame(ownerMembership, result);

        verify(userRepository)
                .findByEmail("owner@example.com");

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        owner.getId()
                );
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authorizationService.getCurrentMember(
                                1L,
                                "unknown@example.com"
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("unknown@example.com");

        verify(tripMemberRepository, never())
                .findByTripIdAndUserId(
                        1L,
                        1L
                );
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotTripMember() {

        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                member.getId()
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authorizationService.getCurrentMember(
                                1L,
                                "member@example.com"
                        )
                );

        assertEquals(
                "You are not a member of this trip",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("member@example.com");

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        member.getId()
                );
    }

    @Test
    void shouldAllowMember() {

        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                member.getId()
        )).thenReturn(Optional.of(memberMembership));

        authorizationService.requireMember(
                1L,
                "member@example.com"
        );

        verify(userRepository)
                .findByEmail("member@example.com");

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        member.getId()
                );
    }

    @Test
    void shouldAllowOwnerAsMember() {

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                owner.getId()
        )).thenReturn(Optional.of(ownerMembership));

        authorizationService.requireMember(
                1L,
                "owner@example.com"
        );

        verify(userRepository)
                .findByEmail("owner@example.com");

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        owner.getId()
                );
    }

    @Test
    void shouldRejectNonMember() {

        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                member.getId()
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authorizationService.requireMember(
                                1L,
                                "member@example.com"
                        )
                );

        assertEquals(
                "You are not a member of this trip",
                exception.getMessage()
        );
    }

    @Test
    void shouldAllowOwner() {

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                owner.getId()
        )).thenReturn(Optional.of(ownerMembership));

        authorizationService.requireOwner(
                1L,
                "owner@example.com"
        );

        verify(userRepository)
                .findByEmail("owner@example.com");

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        owner.getId()
                );
    }

    @Test
    void shouldRejectMemberWhenOwnerIsRequired() {

        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                member.getId()
        )).thenReturn(Optional.of(memberMembership));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authorizationService.requireOwner(
                                1L,
                                "member@example.com"
                        )
                );

        assertEquals(
                "Only the trip owner can perform this action",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("member@example.com");

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        member.getId()
                );
    }

    @Test
    void shouldRejectNonMemberWhenOwnerIsRequired() {

        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                member.getId()
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authorizationService.requireOwner(
                                1L,
                                "member@example.com"
                        )
                );

        assertEquals(
                "You are not a member of this trip",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUnknownUserWhenOwnerIsRequired() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authorizationService.requireOwner(
                                1L,
                                "unknown@example.com"
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(tripMemberRepository, never())
                .findByTripIdAndUserId(
                        1L,
                        1L
                );
    }
}