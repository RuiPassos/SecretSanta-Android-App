package com.example.secretsanta.ui.navigation

import kotlinx.serialization.Serializable

/** Id used in routes to say "create a new item". Room ids start at 1. */
const val NEW_ID = 0L

@Serializable
data object GroupsRoute

@Serializable
data class GroupEditRoute(val groupId: Long = NEW_ID)

@Serializable
data class GroupDetailRoute(val groupId: Long)

@Serializable
data class ParticipantEditRoute(val groupId: Long, val participantId: Long = NEW_ID)

@Serializable
data class RevealRoute(val groupId: Long)
