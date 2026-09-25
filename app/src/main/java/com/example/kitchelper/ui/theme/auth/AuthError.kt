package com.example.kitchelper.ui.theme.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AuthError( // Funcion para resivir parametros tales como el mensaje y la accion de cerrar
    message: String,
    onDismiss: () -> Unit
) {
    Dialog( // creacion de la ventana emergente
        onDismissRequest = onDismiss
    ) {
        Box( // personalizacion de la ventana emergente  y  caja princial
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(40.dp)
                )
                .border(
                    width = 6.dp,
                    color = Color(0xFFD2DC83),
                    shape = RoundedCornerShape(40.dp)
                )
                .padding(30.dp)
        ) {
            // Se crea el contorno central para ingresar el icono el texto y el boton
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // lugar para crear la X roja  dentro del contorno
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFFFF4038),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon( //iconografia
                        imageVector = Icons.Default.Close,
                        contentDescription = "Error",
                        tint = Color.White,
                        modifier = Modifier.size(70.dp)
                    )
                }

                // ESPACIADO ENTRE EL ICONO Y EL TEXTO
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                // Se crea el espacio para el texto dinamcio
                Text(

                    text = message,
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    color = Color.Black
                )
                // ESPACIADO ENTRE EL TEXTO Y EL BOTON
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                // Se crea el boton aceptar el error dentro de la columna
                Button(
                    onClick = onDismiss
                ) {
                    Text(
                        text = "ACEPTAR"
                    )
                }
            }
        }

    }
}