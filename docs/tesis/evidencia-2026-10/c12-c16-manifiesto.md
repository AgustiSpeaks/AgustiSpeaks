# Criterios 12 y 16 (parte A) — manifiesto combinado

**Fuente:** `app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`,
generado por Gradle el 2026-10-06 a las 13:46. Copia completa en
[`c12-c16-manifiesto-combinado-debug.xml`](c12-c16-manifiesto-combinado-debug.xml).

**Código:** `a0accaf`. El manifiesto fuente (`app/src/main/AndroidManifest.xml`)
no cambia desde el commit `cbe46e9` (5-10, 18:56), anterior a `a0accaf`.

Leído el 2026-10-06.

## Criterio 12 — Permisos

`<uses-permission>` presentes en el manifiesto combinado:

```xml
<uses-permission android:name="com.tunombre.agustispeaks.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" />
```

Declarado por la biblioteca `androidx.core`:

```xml
<permission
    android:name="com.tunombre.agustispeaks.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
    android:protectionLevel="signature" />
```

Es un permiso **propio de la app**, de nivel `signature`: protege los receptores
de difusión que la app registra en tiempo de ejecución, y solo lo pueden usar
apps firmadas con la misma llave. No es de red ni de cámara.

Búsqueda explícita en el manifiesto combinado:

| Permiso | Resultado |
|---|---|
| `android.permission.INTERNET` | no aparece |
| `android.permission.ACCESS_NETWORK_STATE` | no aparece |
| `android.permission.ACCESS_WIFI_STATE` | no aparece |
| `android.permission.CAMERA` | no aparece |

**Permisos de red o cámara: 0.**

## Criterio 16, parte A — Configuración de respaldo

En `<application>` del manifiesto combinado:

```xml
android:allowBackup="false"
android:dataExtractionRules="@xml/data_extraction_rules"
android:fullBackupContent="@xml/backup_rules"
```

Las reglas de extracción (copia en
[`c16-data_extraction_rules.xml`](c16-data_extraction_rules.xml)) excluyen
`root`, `file`, `database`, `sharedpref` y `external`, tanto en
`<cloud-backup>` como en `<device-transfer>`.
