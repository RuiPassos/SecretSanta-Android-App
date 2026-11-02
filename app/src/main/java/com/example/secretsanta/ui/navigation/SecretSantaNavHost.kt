package com.example.secretsanta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.secretsanta.ui.groupdetail.GroupDetailScreen
import com.example.secretsanta.ui.groupedit.GroupEditScreen
import com.example.secretsanta.ui.groups.GroupsScreen
import com.example.secretsanta.ui.participant.ParticipantEditScreen
import com.example.secretsanta.ui.reveal.RevealScreen

@Composable
fun SecretSantaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = GroupsRoute,
        modifier = modifier,
    ) {
        composable<GroupsRoute> {
            GroupsScreen(
                onCreateGroup = { navController.navigate(GroupEditRoute()) },
                onOpenGroup = { navController.navigate(GroupDetailRoute(it)) },
            )
        }
        composable<GroupEditRoute> { entry ->
            GroupEditScreen(
                route = entry.toRoute(),
                onBack = navController::navigateUp,
                onGroupCreated = { groupId ->
                    navController.navigate(GroupDetailRoute(groupId)) {
                        popUpTo<GroupEditRoute> { inclusive = true }
                    }
                },
            )
        }
        composable<GroupDetailRoute> { entry ->
            GroupDetailScreen(
                route = entry.toRoute(),
                onBack = navController::navigateUp,
                onEditGroup = { navController.navigate(GroupEditRoute(it)) },
                onAddParticipant = { navController.navigate(ParticipantEditRoute(it)) },
                onOpenParticipant = { groupId, participantId ->
                    navController.navigate(ParticipantEditRoute(groupId, participantId))
                },
                onReveal = { navController.navigate(RevealRoute(it)) },
            )
        }
        composable<ParticipantEditRoute> { entry ->
            ParticipantEditScreen(route = entry.toRoute(), onBack = navController::navigateUp)
        }
        composable<RevealRoute> { entry ->
            RevealScreen(route = entry.toRoute(), onBack = navController::navigateUp)
        }
    }
}
