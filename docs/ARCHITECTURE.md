# Arquitectura de DiosesDrakes

## Proposito

DiosesDrakes convierte dinero acumulado en decisiones jugables mediante desbloqueos,
mantenimiento semanal, ofrendas de materiales y poderes limitados.

No entrega objetos desbalanceados ni reemplaza Vault.

## Responsabilidades

| Componente | Responsabilidad |
| --- | --- |
| DiosesDrakes | Perfiles, arboles, poderes, costos, mantenimiento y GUI. |
| sBank | Auditoria de movimientos y tesoreria. |
| Vault | Retiro y consulta de dinero. |
| Odysseia | Administracion, webhook y eventos globales. |
| Slimefun y addons | Objetos, recetas, maquinas y redes permitidas. |
| AuraSkills | Estadisticas pasivas; no niveles divinos. |
| WorldGuard y ProtectionStones | Limites de territorio que nunca se omiten. |

## Reglas no negociables

1. Un jugador mantiene un solo dios activo.
2. Renunciar elimina el progreso del dios actual y aplica 48 horas de espera.
3. El mantenimiento es semanal y conserva progreso durante una gracia breve.
4. Costos y efectos sensibles se auditan con identificadores unicos.
5. Las habilidades se niegan ante una proteccion ajena o integracion no permitida.
6. PvP normal desactiva poderes divinos. PvPDivino progresa solo jugando arena.
7. Ninguna habilidad duplica items, energia, experiencia o dinero.

## Lanzamiento por etapas

1. Nucleo: perfiles SQLite, transacciones atomicas, auditoria, GUI y adaptadores. Implementado.
2. Hefesto: industria, mineria, metalurgia y energia con listas Slimefun permitidas. Implementado como piloto.
3. PvPDivino: region segura, progresion por combate y arena sobre controles reales.
4. Expansion: un dios por iteracion, con documentacion y balance antes de continuar.

## Hefesto en produccion

- Los desbloqueos verifican dios activo, prerrequisitos y coste antes de retirar dinero por Vault.
- Un cobro comprometido se identifica por detalle y se reutiliza tras una interrupcion, sin cobrar dos veces.
- Cada vencimiento semanal tiene su propio identificador. La gracia es configurable; al agotarse, solo se
  suspende la carga, no se borra el arbol.
- Pulso de Red usa reflexion aislada contra la API de Slimefun para que el plugin siga cargando si Slimefun falta.
  Solo acepta IDs presentes en la lista permitida y comprueba WorldGuard antes de tocar energia.
- Ojo de Mena solo emite cambios visuales temporales al cliente en bloques donde el jugador puede interactuar.
