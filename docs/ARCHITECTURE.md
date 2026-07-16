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

1. Nucleo: perfiles SQLite, transacciones atomicas, auditoria, GUI y adaptadores.
2. Hefesto: industria, mineria, metalurgia y energia con listas Slimefun permitidas.
3. PvPDivino: region segura, progresion por combate y arena sobre controles reales.
4. Expansion: un dios por iteracion, con documentacion y balance antes de continuar.
