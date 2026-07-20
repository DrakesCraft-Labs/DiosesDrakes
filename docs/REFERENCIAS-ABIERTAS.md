# Referencias abiertas y decisiones de diseno

Esta investigacion evita reinventar las piezas repetibles de un sistema de
habilidades, sin importar codigo ni dependencias que condicionen la licencia de
DiosesDrakes.

## Fuentes revisadas

| Proyecto | Licencia revisada | Patron aprovechable | Decision |
| --- | --- | --- | --- |
| [AuraSkills](https://github.com/Archy-X/AuraSkills) | GPL-3.0 | Habilidades separadas en configuracion, coste, enfriamiento y nivel. API de eventos para estadisticas. | Solo referencia conceptual y posible integracion por API. No copiar codigo. |
| [AureliumSkills historico](https://github.com/SkyFull-eu/AureliumSkills) | MIT | Catalogo declarativo, proveedores de almacenamiento y adaptadores de habilidad. | Referencia permisiva; aun asi, preferimos una implementacion propia y pequena. |
| [EcoSkills](https://github.com/Auxilor/EcoSkills) | GPL-3.0 | Efectos definidos por archivos, progresion por fuentes y GUI informativa. | Solo referencia conceptual. No copiar codigo ni archivos. |
| [Slimefun4](https://github.com/Slimefun/Slimefun4) | GPL-3.0 | Integraciones por API y validacion explicita de objetos. | Integrar solamente mediante API publica y listas permitidas del servidor. |

## Modelo que adopta DiosesDrakes

Cada habilidad se describe como datos, no como una entrada fija en una clase:

```yaml
id: hephaestus.forja_viva
branch: metallurgy
type: passive
tier: 1
requires: []
activation:
  cooldown-seconds: 0
  duration-seconds: 0
  slots: 0
effect: smelting_xp_bonus
limits:
  pvp: disabled
  protection: owner-or-member
```

Las ramas son un grafo dirigido sin ciclos: una habilidad declara sus
prerrequisitos, coste, tipo y adaptador de efecto. Esto permite sumar contenido
sin cambiar los servicios centrales ni entregar todas las bendiciones a la vez.

## Reglas de implementacion

1. `SkillDefinition` debe cargarse desde archivos de dioses y validarse al inicio.
2. `SkillService` es la unica puerta para comprobar dios activo, desbloqueo,
   mantenimiento, carga equipada, coste y cooldown.
3. Cada efecto se implementa como un adaptador propio con una lista permitida de
   acciones, materiales, regiones y APIs. Un adaptador inexistente deja la
   habilidad visible como planificada, nunca como poder ficticio.
4. Pasivas no consumen un hueco. Activas y posturas tienen cupos separados,
   cooldown visible y mensajes de estado accionables.
5. AuraSkills conserva sus niveles y XP. DiosesDrakes registra solo devocion y
   progreso de arena PvPDivino, sin duplicar experiencia.
6. Vault y sBank reciben transacciones idempotentes y auditadas; fallar una
   integracion niega la compra o la renovacion, no concede un beneficio gratis.
7. WorldGuard, ProtectionStones, AxGraves y Slimefun se consultan antes de
   modificar mundo, inventarios, tumbas o energia.

## Orden de construccion

1. Cargador y validador de definiciones YAML con pruebas de ramas y ciclos.
2. Economia, desbloqueo transaccional, mantenimiento semanal y limites de carga.
3. Hefesto completo con adaptadores seguros de fundicion y maquinas permitidas.
4. Un dios por iteracion, con efectos reales, pruebas y documentacion para
   jugadores antes de publicar la siguiente rama.

