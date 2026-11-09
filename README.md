# BetterCrates

Plugin de crates personalizables para Spigot 1.12.2.

## Requisitos
- Java 8
- Maven
- API de Spigot 1.12.2

## Compilar
Ejecuta desde la raiz del repositorio:

```bash
mvn clean package
```

El archivo compilado se genera en `target/BetterCrates.jar`.

## Comandos iniciales
- `/bc help`
- `/bc list`
- `/bc create <id>`
- `/bc reload`

## Permisos
- `bettercrates.admin`: administracion del plugin.
- `bettercrates.use`: permiso reservado para utilizar crates.

## Estado
Base inicial: carga de configuracion, mensajes, creacion/listado de crates y guardado en `crates.yml`.
