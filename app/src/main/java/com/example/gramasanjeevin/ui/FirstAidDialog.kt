package com.example.gramasanjeevin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramasanjeevin.utils.L

/**
 * A pop-up dialog showing First-Aid basics for villagers.
 * Features a scrollable list of informative cards with language support.
 */
@Composable
fun FirstAidDialog(
    authViewModel: AuthViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val isEnglish by authViewModel.isEnglish.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFF1976D2))
                    Spacer(Modifier.width(8.dp))
                    Text(L.firstAid(isEnglish), fontWeight = FontWeight.Bold, color = Color.Black)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = L.close(isEnglish), tint = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                FirstAidTipCard(
                    title = L.s(isEnglish, "Snake Bite", "ಹಾವಿನ ಕಡಿತ"),
                    instructions = L.s(isEnglish, 
                        "• Keep the person calm and still.\n• Immobilize the limb and keep it below heart level.\n• Do NOT use a tourniquet or cut the wound.\n• Seek immediate medical help.",
                        "• ವ್ಯಕ್ತಿಯನ್ನು ಶಾಂತವಾಗಿ ಮತ್ತು ಸ್ಥಿರವಾಗಿರಿಸಿ.\n• ಪೆಟ್ಟಾದ ಭಾಗವನ್ನು ಹೃದಯದ ಮಟ್ಟಕ್ಕಿಂತ ಕೆಳಗಿರಿಸಿ.\n• ಗಾಯವನ್ನು ಕತ್ತರಿಸಬೇಡಿ ಅಥವಾ ಬಿಗಿಯಾಗಿ ಕಟ್ಟಬೇಡಿ.\n• ತಕ್ಷಣ ವೈದ್ಯಕೀಯ ಸಹಾಯ ಪಡೆಯಿರಿ."
                    ),
                    containerColor = Color(0xFFFFF3E0)
                )
                
                Spacer(Modifier.height(12.dp))
                
                FirstAidTipCard(
                    title = L.s(isEnglish, "Minor Burns", "ಸಣ್ಣ ಸುಟ್ಟಗಾಯಗಳು"),
                    instructions = L.s(isEnglish,
                        "• Run cool (not cold) water over the burn for 10-20 minutes.\n• Do NOT apply ice, butter, or ointments.\n• Cover loosely with a sterile bandage.",
                        "• ಸುಟ್ಟ ಗಾಯದ ಮೇಲೆ 10-20 ನಿಮಿಷಗಳ ಕಾಲ ತಣ್ಣನೆಯ ನೀರನ್ನು ಸುರಿಯಿರಿ.\n• ಮಂಜುಗಡ್ಡೆ, ಬೆಣ್ಣೆ ಅಥವಾ ಮುಲಾಮುಗಳನ್ನು ಹಚ್ಚಬೇಡಿ.\n• ಕ್ರಿಮಿಮುಕ್ತ ಬ್ಯಾಂಡೇಜ್‌ನಿಂದ ಸಡಿಲವಾಗಿ ಮುಚ್ಚಿ."
                    ),
                    containerColor = Color(0xFFE3F2FD)
                )
                
                Spacer(Modifier.height(12.dp))
                
                FirstAidTipCard(
                    title = L.s(isEnglish, "Heat Stroke", "ಬಿಸಿಲು ಹೊಡೆತ"),
                    instructions = L.s(isEnglish,
                        "• Move the person to a cool, shaded area.\n• Cool them down with wet cloths or cool water.\n• Do NOT give fluids if they are unconscious.",
                        "• ವ್ಯಕ್ತಿಯನ್ನು ತಂಪಾದ, ನೆರಳಿನ ಪ್ರದೇಶಕ್ಕೆ ಸ್ಥಳಾಂತರಿಸಿ.\n• ಒದ್ದೆಯಾದ ಬಟ್ಟೆ ಅಥವಾ ತಣ್ಣನೆಯ ನೀರಿನಿಂದ ದೇಹವನ್ನು ತಂಪು ಮಾಡಿ.\n• ಪ್ರಜ್ಞಾಹೀನರಾಗಿದ್ದರೆ ದ್ರವ ಪದಾರ್ಥಗಳನ್ನು ನೀಡಬೇಡಿ."
                    ),
                    containerColor = Color(0xFFE8F5E9)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(L.close(isEnglish), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
private fun FirstAidTipCard(title: String, instructions: String, containerColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = instructions,
                color = Color(0xFF333333),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
