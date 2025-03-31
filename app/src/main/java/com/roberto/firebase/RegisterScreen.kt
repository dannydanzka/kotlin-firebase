package com.roberto.firebase.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.roberto.firebase.R

@Composable
fun RegisterScreen(
    uidEmpleadoEditando: String? = null,
    onGoToLista: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance().reference

    var numero by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var sueldo by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isEditing = uidEmpleadoEditando != null

    // Cargar datos del empleado si es edición
    LaunchedEffect(uidEmpleadoEditando) {
        if (isEditing) {
            db.child("empleados").child(uidEmpleadoEditando!!).get().addOnSuccessListener {
                numero = it.child("numero").value?.toString() ?: ""
                nombre = it.child("nombre").value?.toString() ?: ""
                apellidos = it.child("apellidos").value?.toString() ?: ""
                email = it.child("correo").value?.toString() ?: ""
                sueldo = it.child("sueldo").value?.toString() ?: ""
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (isEditing) "Editar Empleado" else "Registro de Empleado",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.employee_image),
            contentDescription = "Employee",
            modifier = Modifier.size(100.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = numero, onValueChange = { numero = it }, label = { Text("Número") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(value = sueldo, onValueChange = { sueldo = it }, label = { Text("Sueldo") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                errorMessage = null

                if (numero.isBlank() || nombre.isBlank() || apellidos.isBlank() || email.isBlank() || sueldo.isBlank() || (!isEditing && password.isBlank())) {
                    errorMessage = "Todos los campos son obligatorios."
                    return@Button
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "Correo inválido."
                    return@Button
                }

                if (!isEditing && password.length < 6) {
                    errorMessage = "Contraseña mínima de 6 caracteres."
                    return@Button
                }

                loading = true

                if (isEditing) {
                    val empleado = mapOf(
                        "numero" to numero,
                        "nombre" to nombre,
                        "apellidos" to apellidos,
                        "correo" to email,
                        "sueldo" to sueldo
                    )
                    db.child("empleados").child(uidEmpleadoEditando!!).updateChildren(empleado)
                        .addOnCompleteListener {
                            if (password.isNotBlank()) {
                                auth.currentUser?.updatePassword(password)
                            }
                            loading = false
                            Toast.makeText(context, "Empleado actualizado", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                                val empleado = mapOf(
                                    "numero" to numero,
                                    "nombre" to nombre,
                                    "apellidos" to apellidos,
                                    "correo" to email,
                                    "sueldo" to sueldo
                                )
                                db.child("empleados").child(uid).setValue(empleado)
                                    .addOnCompleteListener {
                                        loading = false
                                        Toast.makeText(context, "Empleado registrado", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                loading = false
                                errorMessage = task.exception?.localizedMessage
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (isEditing) "Actualizar" else "Registrar")
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar empleado")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onGoToLista, modifier = Modifier.fillMaxWidth()) {
            Text("Ver lista de empleados")
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onLogout) {
            Text("Cerrar sesión")
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar empleado?") },
            text = { Text("Este registro se eliminará permanentemente. ¿Estás seguro?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    loading = true
                    db.child("empleados").child(uidEmpleadoEditando!!).removeValue()
                        .addOnCompleteListener {
                            loading = false
                            Toast.makeText(context, "Empleado eliminado", Toast.LENGTH_SHORT).show()
                        }
                }) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
