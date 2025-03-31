# Firebase Employee Manager — Android (Jetpack Compose + Firebase)

Aplicación Android moderna para registro y gestión de empleados con autenticación y base de datos en tiempo real usando Firebase.

---

## 🧱 Tech Stack

- 🔥 Firebase Authentication
- 🔥 Firebase Realtime Database
- 🧭 Jetpack Compose
- 🧪 Kotlin DSL (`build.gradle.kts`)
- 🧭 Navigation Compose

---

## 🧑‍💻 Funcionalidades

### ✅ Login
- Autenticación con email y contraseña
- Validación de campos
- Redirección a gestión de empleados

### ✅ Registro de Empleados
- Campos: número, nombre, apellidos, correo, sueldo, contraseña
- Registro de empleados en Auth y Realtime DB (`empleados_por_numero/{numero}`)
- Creación, búsqueda, actualización (y opcionalmente eliminación)
- Contraseña obligatoria solo en creación
- Correo no editable en edición
- Todo validado con mensajes en español

### ✅ Lista de empleados
- Solo lectura
- Muestra todos los empleados sin contraseña
- Sin interacción/clics

---

## 🛠 Instalación

1. Clonar este repositorio
2. Abrir en Android Studio
3. Agregar tu `google-services.json` a `/app`
4. Verificar conexión a Firebase en:
   - Firebase Authentication (habilitar Email/Password)
   - Realtime Database (activar y revisar reglas temporales)

---

## 🔐 Reglas para Realtime Database (solo desarrollo)

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}

🧪 Usuario de prueba

Correo: user@mail.com
Contraseña: 123456

🧩 Navegación

LoginScreen
   ↓ onLoginSuccess
RegisterScreen (crear / buscar / editar / actualizar)
   ↓
EmployeesListScreen (solo lectura)

🖼 Recursos

res/drawable/user_image.png — ícono login
res/drawable/employee_image.png — ícono empleados
res/drawable/*.xml/png — iconos para botones: buscar, crear, actualizar, eliminar

📂 Estructura de datos en Firebase

empleados_por_numero: {
  "001": {
    "nombre": "Juan",
    "apellidos": "Pérez",
    "correo": "juan@mail.com",
    "sueldo": "10000"
  }
}
