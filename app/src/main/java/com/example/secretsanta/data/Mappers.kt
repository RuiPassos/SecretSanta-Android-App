package com.example.secretsanta.data

import com.example.secretsanta.data.local.entity.GroupEntity
import com.example.secretsanta.data.local.entity.GroupWithCount
import com.example.secretsanta.data.local.entity.ParticipantEntity
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.Participant

fun GroupEntity.toDomain(participantCount: Int = 0) = Group(
    id = id,
    name = name,
    mode = mode,
    eventDate = eventDate,
    budget = budget,
    location = location,
    notes = notes,
    createdAt = createdAt,
    drawnAt = drawnAt,
    participantCount = participantCount,
)

fun GroupWithCount.toDomain() = group.toDomain(participantCount)

fun Group.toEntity() = GroupEntity(
    id = id,
    name = name.trim(),
    mode = mode,
    eventDate = eventDate,
    budget = budget.trim(),
    location = location.trim(),
    notes = notes.trim(),
    createdAt = createdAt,
    drawnAt = drawnAt,
)

fun ParticipantEntity.toDomain(excludedIds: Set<Long> = emptySet()) = Participant(
    id = id,
    groupId = groupId,
    name = name,
    email = email,
    phone = phone,
    wishes = wishes,
    excludedIds = excludedIds,
)

fun Participant.toEntity() = ParticipantEntity(
    id = id,
    groupId = groupId,
    name = name.trim(),
    email = email.trim(),
    phone = phone.trim(),
    wishes = wishes.trim(),
)
