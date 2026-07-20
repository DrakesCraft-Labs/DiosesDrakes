# La Convergencia

La Convergencia es el sistema permanente que conecta los panteones con los bosses
de DrakesCraft. No usa temporadas, no resetea mapas, no limpia progreso global y no
convierte el survival en PvP. Su proposito es sacar dinero y favor acumulado de la
circulacion a traves de decisiones visibles y sostenidas.

## Panteones disponibles

| Panteon | Identidad | Patronos iniciales |
| --- | --- | --- |
| Olimpo y Titanes | Orden, destino, mar, guerra, submundo y tecnica. | Olimpicos, dioses menores y doce Titanes. |
| Asgard | Runas, juramentos, tormenta, caceria y Ragnarok. | Thor, Odin, Loki, Heimdall, Freyja, Tyr. |
| Duat | Ciclo solar, juicio, muerte, desierto y retorno. | Ra, Anubis, Isis, Set, Bastet, Horus. |
| Tuatha de Danann | Bruma, metamorfosis, naturaleza y pactos. | Morrigan, Lugh, Brigid, Cernunnos, Dagda. |

Un jugador elige panteon y patron desde `/dioses`. Solo puede mantener una senda.
Al renunciar se borran favor, nodos, carga y reliquias del patron actual; hay 48
horas de espera antes del siguiente juramento. El mundo y el estado global de las
anclas permanecen intactos.

## Anclas permanentes

El staff construye físicamente las plazas y luego registra el punto desde el bloque
elegido:

```text
/dioses ancla crear olimpo GREEK
/dioses ancla crear asgard NORDIC
/dioses ancla crear umbral EGYPTIAN
```

El tercer argumento define el dominio inicial narrativo. No concede propiedad,
permisos ni modificaciones de terreno. Las coordenadas se guardan en SQLite bajo
`plugins/DiosesDrakes/diosesdrakes.db`; el plugin no intenta encontrar, cargar ni
modificar chunks lejanos.

Los jugadores consultan `/dioses ancla lista` y `/dioses ancla estado <id>`. Para
apoyar una faccion usan `/dioses ancla ofrendar <id> <favor>`. El favor se obtiene
por bosses de Odysseia y se debita de la senda activa antes de incrementar el favor
publico. Una ofrenda menor al minimo configurado no se acepta. El dominio solo rota
cuando el nuevo lider supera al actual por el margen de configuracion.

## Bosses e integracion

Odysseia conserva toda la IA, fases, drops y control de bosses. Al morir un boss,
Odysseia publica una victoria con identificador idempotente; DiosesDrakes entrega
favor una sola vez a cada participante elegible. La configuracion asigna afinidad:
Thor, Odin, Loki y Heimdall favorecen Asgard; Zeus, Kratos y Tifon favorecen Olimpo.
La afinidad mejora el favor del panteon correspondiente, pero no impide que otros
participen.

Star solo recibe telemetria firmada. No entrega items, rangos ni favor. Tebex sigue
la ruta Tebex -> consola -> Odysseia, aislada de esta progresion.

## PvP y claims

Las tecnicas contra jugadores solo se habilitan dentro de la region WorldGuard
`pvpdivino`. Las anclas no activan PvP por estar cerca. Poderes, particulas,
explosiones visuales y movilidad respetan WorldGuard y ProtectionStones: no se
abren cofres, no se rompen bloques, no se cambia clima global y no se ejecuta daño
entre jugadores en survival.

## Operacion segura

1. Construye tres plazas publicas y define sus nombres cortos.
2. Registra las anclas con una cuenta `diosesdrakes.admin`.
3. Verifica estado y una ofrenda de prueba con un jugador de cada panteon.
4. Activa el JAR solo en un reinicio ya planificado; no hace falta reiniciar ni
   regenerar el mundo para crear, ajustar o consultar anclas despues.

Los panteones posteriores deben incorporar patronos, habilidades, afinidades de
boss y una identidad jugable propia antes de habilitarse en `PantheonId`.
