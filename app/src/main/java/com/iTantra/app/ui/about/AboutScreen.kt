package com.iTantra.app.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iTantra.app.ui.theme.*

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ITantraBackground)
            .padding(horizontal = 22.dp)
    ) {

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =====================================================
        // HEADER
        // =====================================================

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ITantraText
                )
            }

            Text(
                text = "About iTANTRA",
                color = ITantraText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }


        Spacer(
            modifier = Modifier.height(50.dp)
        )


        // =====================================================
        // APP NAME
        // =====================================================

        Text(
            text = "iTANTRA",
            color = ITantraGreen,
            fontSize = 34.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )


        Spacer(
            modifier = Modifier.height(4.dp)
        )


        Text(
            text = "Offline Multilingual Communication",
            color = ITantraSecondaryText,
            fontSize = 14.sp,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text = "Version 1.0.0",
            color = ITantraSecondaryText,
            fontSize = 12.sp,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )


        Spacer(
            modifier = Modifier.height(48.dp)
        )


        // =====================================================
        // ABOUT CARD
        // =====================================================

        Surface(
            color = ITantraSurface,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "What is iTANTRA?",
                    color = ITantraText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )


                Spacer(
                    modifier = Modifier.height(12.dp)
                )


                Text(
                    text = "iTANTRA enables multilingual voice communication without internet or cellular connectivity by converting speech into lightweight text, transmitting it between nearby devices, and reconstructing speech on the receiving device.",
                    color = ITantraSecondaryText,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }


        Spacer(
            modifier = Modifier.height(22.dp)
        )


        // =====================================================
        // ISRO PROBLEM STATEMENT
        // =====================================================

        Surface(
            color = ITantraLightGreen,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "ISRO Problem Statement",
                    color = ITantraSecondaryText,
                    fontSize = 12.sp
                )


                Spacer(
                    modifier = Modifier.height(5.dp)
                )


                Text(
                    text = "26173",
                    color = ITantraDarkGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }
    }
}