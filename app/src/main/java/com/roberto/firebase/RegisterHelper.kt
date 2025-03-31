package com.roberto.firebase.helpers

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

data class EmpleadoData(
    val numero: String,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val sueldo: String,
    val password: String
)

fun limpiarCampos(
    setNumero: (String) -> Unit,
    setNombre: (String) -> Unit,
    setApellidos: (String) -> Unit,
    setCorreo: (String) -> Unit,
    setSueldo: (String) -> Unit,
    setPassword: (String) -> Unit,
    setModoEdicion: (Boolean) -> Unit
) {
    setNumero("")
    setNombre("")
    setApellidos("")
    setCorreo("")
    setSueldo("")
    setPassword("")
    setModoEdicion(false)
}

fun validarEmpleado(
    context: Context,
    datos: EmpleadoData,
    modoEdicion: Boolean,
    setError: (String?) -> Unit
): Boolean {
    if (
        datos.numero.isBlank() || datos.nombre.isBlank() || datos.apellidos.isBlank() ||
        datos.sueldo.isBlank() || datos.correo.isBlank() || (!modoEdicion && datos.password.isBlank())
    ) {
        setError("Todos los campos son obligatorios.")
        return false
    }

    if (!Patterns.EMAIL_ADDRESS.matcher(datos.correo).matches()) {
        setError("Correo electrónico inválido.")
        return false
    }

    if (!modoEdicion && datos.password.length < 6) {
        setError("La contraseña debe tener al menos 6 caracteres.")
        return false
    }

    if (modoEdicion && datos.password.isNotBlank() && datos.password.length < 6) {
        setError("Contraseña inválida: mínimo 6 caracteres.")
        return false
    }

    setError(null)
    return true
}

fun guardarEmpleadoEnFirebase(
    db: DatabaseReference,
    datos: EmpleadoData,
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(datos.correo, datos.password)
        .addOnSuccessListener {
            val empleado = mapOf(
                "numero" to datos.numero,
                "nombre" to datos.nombre,
                "apellidos" to datos.apellidos,
                "correo" to datos.correo,
                "sueldo" to datos.sueldo
            )
            db.child("empleados_por_numero").child(datos.numero).setValue(empleado)
                .addOnCompleteListener {
                    Toast.makeText(context, "Empleado registrado", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
        }
        .addOnFailureListener {
            onError(it.localizedMessage ?: "Error al registrar el usuario")
        }
}

fun buscarEmpleadoPorNumero(
    db: DatabaseReference,
    numero: String,
    onResult: (nombre: String, apellidos: String, correo: String, sueldo: String) -> Unit,
    onNotFound: () -> Unit
) {
    db.child("empleados_por_numero").child(numero).get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val nombre = snapshot.child("nombre").value?.toString() ?: ""
                val apellidos = snapshot.child("apellidos").value?.toString() ?: ""
                val correo = snapshot.child("correo").value?.toString() ?: ""
                val sueldo = snapshot.child("sueldo").value?.toString() ?: ""
                onResult(nombre, apellidos, correo, sueldo)
            } else {
                onNotFound()
            }
        }
}

fun actualizarEmpleado(
    db: DatabaseReference,
    datos: EmpleadoData,
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val empleado = mapOf(
        "numero" to datos.numero,
        "nombre" to datos.nombre,
        "apellidos" to datos.apellidos,
        "correo" to datos.correo,
        "sueldo" to datos.sueldo
    )

    db.child("empleados_por_numero").child(datos.numero).updateChildren(empleado)
        .addOnCompleteListener {
            if (datos.password.isNotBlank()) {
                FirebaseAuth.getInstance().currentUser?.updatePassword(datos.password)
            }
            Toast.makeText(context, "Empleado actualizado", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener {
            onError(it.localizedMessage ?: "Error al actualizar datos")
        }
}

fun eliminarEmpleado(
    db: DatabaseReference,
    numero: String,
    context: Context,
    onSuccess: () -> Unit
) {
    db.child("empleados_por_numero").child(numero).removeValue()
        .addOnCompleteListener {
            FirebaseAuth.getInstance().currentUser?.delete()
            Toast.makeText(context, "Empleado eliminado", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
}
