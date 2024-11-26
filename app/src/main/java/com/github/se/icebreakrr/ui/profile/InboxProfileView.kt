package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.github.se.icebreakrr.ui.sections.shared.InfoSection

@Composable
fun InboxProfileViewScreen(){
    Column{
        ProfileHeader()
        InfoSection()
    }
}