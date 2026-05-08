# NutriLife 🍎

**NutriLife** es una red social móvil diseñada para conectar a entusiastas de la salud y la nutrición. La aplicación permite a los usuarios compartir contenido, interactuar con otros miembros de la comunidad y seguir el progreso de perfiles enfocados en un estilo de vida saludable.

Este proyecto fue desarrollado originalmente en 2024 para demostrar la integración de servicios de nube en tiempo real con Android Studio.

## 🚀 Características

- **Sistema de Autenticación:** Registro e inicio de sesión seguro.
- **Interacción Social:** Sistema de "Likes" y comentarios en publicaciones.
- **Red de Seguidores:** Capacidad para seguir y ser seguido por otros usuarios para personalizar el feed.
- **Gestión de Contenido:** Publicación de posts con imágenes y descripciones.
- **Perfiles Personalizados:** Edición de perfil y visualización de perfiles de terceros.
- **Enfoque Temático:** Interfaz y comunidad orientada exclusivamente al sector de la nutrición.

## 🛠️ Stack Tecnológico

- **Plataforma:** Android Studio (Java/Kotlin).
- **Backend & Base de Datos:** [Firebase Realtime Database / Firestore] - Gestión de datos de usuarios y posts en tiempo real.
- **Autenticación:** Firebase Authentication - Manejo de sesiones y seguridad de cuentas.
- **Almacenamiento de Medios:** Firebase Storage - Guardado y recuperación eficiente de imágenes de perfil y publicaciones.

## ⚙️ Instalación y Configuración

Para correr este proyecto localmente, necesitarás configurar tu propio proyecto en Firebase:

1. Clona el repositorio: `git clone https://github.com/Jesus-Delgado-24/NutriLife.git`
2. Crea un proyecto en el [Firebase Console](https://console.firebase.google.com/).
3. Descarga el archivo `google-services.json` y colócalo en la carpeta `/app`.
4. Habilita **Authentication**, **Database** y **Storage** en tu consola de Firebase.
5. Sincroniza el proyecto con Gradle en Android Studio.

---
Desarrollado por [Jesús Delgado](https://github.com/Jesus-Delgado-24)
