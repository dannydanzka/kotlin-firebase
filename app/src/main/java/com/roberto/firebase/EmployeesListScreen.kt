package com.roberto.firebase.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

data class Empleado(
    val numero: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val sueldo: String = ""
)

@Composable
fun EmployeesListScreen(
    onBack: () -> Unit
) {
    val empleados = remember { mutableStateListOf<Empleado>() }

    LaunchedEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("empleados_por_numero")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                empleados.clear()
                for (child in snapshot.children) {
                    val emp = child.getValue(Empleado::class.java)
                    emp?.let { empleados.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Lista de Empleados", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(empleados) { emp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Número: ${emp.numero}")
                        Text("Nombre: ${emp.nombre} ${emp.apellidos}")
                        Text("Correo: ${emp.correo}")
                        Text("Sueldo: \$${emp.sueldo}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
