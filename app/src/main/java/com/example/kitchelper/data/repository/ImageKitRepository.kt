package com.example.kitchelper.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.InputStream
import java.util.UUID

class ImageKitRepository {

    private val client = OkHttpClient()
    private val publicKey = "public_Iq3206/lPPv48NrkBorMXn2aNQs="
    private val privateKey = "private_B0TPv91wdV0CW8Qi7fJu7N7Cki0=" 
    private val uploadUrl = "https://upload.imagekit.io/api/v1/files/upload"

    /**
     * Subir una imagen o archivo (.glb/.gltf/.jpg/.png) desde una Uri local a ImageKit.io
     * Devuelve la URL CDN pública devuelta por ImageKit.
     * Soporta especificar la carpeta exacta como `/Kitchelper/Recetas`, `/Kitchelper/Recetas/Ingredientes`, `/Kitchelper/Practicas`, etc.
     */
    suspend fun subirArchivo(
        context: Context,
        uri: Uri,
        nombreArchivoPrefijo: String = "archivo",
        folder: String = "/Kitchelper/Recetas"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (privateKey.isBlank()) {
                return@withContext Result.failure(
                    Exception("Falta configurar la Private Key de ImageKit.io.")
                )
            }

            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@withContext Result.failure(
                Exception("No se pudieron leer los datos del archivo local")
            )
            inputStream.close()

            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val fileExtension = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                mimeType.contains("gltf") || mimeType.contains("glb") -> "glb"
                else -> "jpg"
            }
            val fileName = "${nombreArchivoPrefijo}_${UUID.randomUUID()}.$fileExtension"
            val cleanFolder = if (folder.startsWith("/")) folder else "/$folder"

            // Base64 puro de los bytes del archivo
            val base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)

            // Petición Multipart pasando la cadena Base64 que requiere ImageKit API
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", base64Data)
                .addFormDataPart("fileName", fileName)
                .addFormDataPart("publicKey", publicKey)
                .addFormDataPart("useUniqueFileName", "true")
                .addFormDataPart("folder", cleanFolder)
                .build()

            // Autenticación HTTP Basic requerida por la API de ImageKit mediante Private Key
            val authCredential = Credentials.basic(privateKey + ":", "")

            val request = Request.Builder()
                .url(uploadUrl)
                .header("Authorization", authCredential)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: ""

            if (response.isSuccessful && responseText.isNotBlank()) {
                val json = JSONObject(responseText)
                val cdnUrl = json.optString("url", "")
                if (cdnUrl.isNotBlank()) {
                    Result.success(cdnUrl)
                } else {
                    Result.failure(Exception("ImageKit no devolvió una URL válida"))
                }
            } else {
                val jsonErr = try {
                    val json = JSONObject(responseText)
                    json.optString("message", json.optString("help", responseText))
                } catch (_: Exception) {
                    responseText
                }
                Result.failure(Exception("Error en ImageKit: ${if (jsonErr.isNotBlank()) jsonErr else response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
