# Hoja de ruta del Panteon

## Contrato del sistema

DiosesDrakes es el motor de progreso, carga, cooldowns, transacciones y auditoria.
Odysseia orquesta bosses, anuncios, eventos y administracion. SF Core y addons
consultan bendiciones mediante `DivineAccess` y solo activan maquinas, recetas o
bonificaciones incluidas en una lista permitida.

No hay acceso directo de otros plugins a la base de DiosesDrakes. La integracion
se realiza mediante el servicio Bukkit `DivineAccess`, eventos posteriores y
configuracion versionada.

## Escala hacia 1.000 habilidades

Las 69 bendiciones iniciales son el catalogo de diseno. El objetivo de 1.000
nodos se alcanza con definiciones YAML, no con 1.000 listeners Java:

1. Efectos reutilizables: aura, multiplicador, escaneo, movilidad, ritual,
   proteccion, conversion, recarga, invocacion y postura.
2. Restricciones reutilizables: mundo, region, combate, dios, nivel, item,
   maquina, cooldown, mantenimiento y carga.
3. Cada nodo define costos, ofrendas, prerequisitos, texto de ayuda, sonidos y
   efectos visuales desde la misma fuente que alimenta GUI, comando y README.
4. Ningun nodo se publica sin test de condiciones, coste y rollback.

## Integraciones obligatorias

| Integracion | Entrega |
| --- | --- |
| sBank/Vault | Cobro atomico, ledger y auditoria externa. |
| Odysseia | Eventos divinos, bosses con afinidad y webhooks de hitos. |
| SF Core | Permisos de receta, maquina y energia por listas permitidas. |
| AxGraves | Bendiciones de Hades solo sobre tumbas propias. |
| WorldGuard/ProtectionStones | Denegacion conservadora en territorio ajeno. |
| AuraSkills | Estadisticas pasivas, sin duplicar progreso divino. |

## Orden de entrega

1. Completar Hefesto: XP de fundicion, integracion de maquinas autorizadas y
   escaneo de minerales con protecciones, cooldown y auditoria.
2. Economia: precios por percentil, mantenimiento semanal, tesoro y bridge sBank.
3. Protecciones y PvPDivino: adaptadores regionales y arena sin poderes survival.
4. Expandir dioses uno por uno, primero los que conectan con actividades reales.
5. Migrar el catalogo a YAML y generar automaticamente GUI, guia y README.
