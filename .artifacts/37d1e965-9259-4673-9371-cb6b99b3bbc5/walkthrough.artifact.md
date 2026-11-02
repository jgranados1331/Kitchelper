# Resumen de Cambios - Ocultamiento y Protección de `google-services.json` en Git

Se ha configurado la protección del archivo `google-services.json` para evitar exponer claves de Firebase en sistemas de control de versiones (Git/GitHub):

## Cambios Realizados

1. **Exclusión en Control de Versiones (`.gitignore` & `app/.gitignore`)**:
   - Se agregaron las reglas `google-services.json` y `app/google-services.json` a los archivos `.gitignore` del proyecto.
   - Con esto, Git ignorará automáticamente el archivo de credenciales de Firebase, garantizando que nunca se suba ni exponga en repositorios públicos de Git o GitHub.

2. **Aclaración Oficial sobre las Claves de Firebase en Android**:
   - De acuerdo con la documentación oficial de Firebase: las claves dentro de `google-services.json` son identificadores de cliente Android (no claves secretas de servidor).
   - La seguridad en Firebase está resguardada por las **Reglas de Seguridad de Firestore/Storage** y la restricción por huella digital SHA-1 del paquete de tu app.

## Resultados de Verificación
- **Compilación Kotlin**: Ejecución exitosa de `app:compileDebugKotlin` con **Build finished successfully**.
