package com.roberto.firebase.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusDirection
import com.google.firebase.database.FirebaseDatabase
import com.roberto.firebase.R
import com.roberto.firebase.helpers.*

@Composable
fun RegisterScreen(
    onGoToLista: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().reference
    val focusManager = LocalFocusManager.current

    var numero by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var sueldo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var modoEdicion by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    fun clearAll() {
        limpiarCampos(
            { numero = it }, { nombre = it }, { apellidos = it },
            { correo = it }, { sueldo = it }, { password = it }, { modoEdicion = it }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de Empleados", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.employee_image),
            contentDescription = "Empleado",
            modifier = Modifier.size(100.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Número
        OutlinedTextField(
            value = numero,
            onValueChange = { numero = it },
            label = { Text("Número de empleado") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo Correo
        OutlinedTextField(
            value = correo,
            onValueChange = { if (!modoEdicion) correo = it },
            label = { Text("Correo electrónico") },
            enabled = !modoEdicion,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo Apellidos
        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo Sueldo
        OutlinedTextField(
            value = sueldo,
            onValueChange = { sueldo = it },
            label = { Text("Sueldo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Crear
            IconButton(onClick = {
                val datos = EmpleadoData(numero, nombre, apellidos, correo, sueldo, password)
                if (validarEmpleado(context, datos, modoEdicion) { error = it }) {
                    guardarEmpleadoEnFirebase(db, datos, context,
                        onSuccess = { clearAll() },
                        onError = { error = it }
                    )
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.create_icon),
                    contentDescription = "Crear"
                )
            }

            // Editar
            IconButton(onClick = {
                if (!modoEdicion) {
                    error = "Primero busca un empleado"
                    return@IconButton
                }
                val datos = EmpleadoData(numero, nombre, apellidos, correo, sueldo, password)
                if (validarEmpleado(context, datos, modoEdicion) { error = it }) {
                    actualizarEmpleado(db, datos, context,
                        onSuccess = { clearAll() },
                        onError = { error = it }
                    )
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.update_icon),
                    contentDescription = "Editar"
                )
            }

            // Buscar
            IconButton(onClick = {
                if (numero.isBlank()) {
                    error = "Ingresa el número de empleado"
                    return@IconButton
                }
                buscarEmpleadoPorNumero(db, numero,
                    onResult = { n, a, c, s ->
                        nombre = n
                        apellidos = a
                        correo = c
                        sueldo = s
                        modoEdicion = true
                        password = ""
                        Toast.makeText(context, "Empleado encontrado", Toast.LENGTH_SHORT).show()
                    },
                    onNotFound = {
                        error = "Empleado no encontrado"
                    }
                )
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.find_icon),
                    contentDescription = "Buscar"
                )
            }

            // Eliminar
            IconButton(onClick = {
                if (!modoEdicion) {
                    error = "Busca un empleado primero"
                    return@IconButton
                }
                showConfirmDelete = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.delete_icon),
                    contentDescription = "Eliminar"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onGoToLista, modifier = Modifier.fillMaxWidth()) {
            Text("Ver lista de empleados")
        }

        TextButton(onClick = onLogout) {
            Text("Cerrar sesión")
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("¿Eliminar empleado?") },
            text = { Text("Esta acción eliminará al empleado y su cuenta. ¿Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDelete = false
                    eliminarEmpleado(db, numero, context) { clearAll() }
                }) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
