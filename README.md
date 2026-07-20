# DiosesDrakes

![DiosesDrakes](logo.svg)

Progresion divina y drenajes economicos para **DrakesCraft**. Cada jugador invierte
en un dios, desbloquea poderes utiles y mantiene sus bendiciones activas mediante
dinero, ofrendas y juego real.

> Estado: **Hefesto** es el primer dios jugable. Los demas dioses siguen en
> diseno y no conceden efectos hasta que cada rama tenga pruebas y balance.

## Que resuelve

- Crea destinos voluntarios y entretenidos para el dinero acumulado.
- Conecta progresion con Slimefun, addons, exploracion, maquinas y eventos.
- Mantiene AuraSkills como estadistica pasiva, sin duplicar XP ni arboles.
- Registra costos y efectos sensibles para que staff pueda auditar abusos.
- Respeta ProtectionStones y WorldGuard desde el diseno.

## Economia divina

| Mecanica | Funcion |
| --- | --- |
| Desbloqueo | Pago unico para obtener un nodo del arbol. |
| Mantenimiento | Cuota semanal mientras una bendicion este activa. |
| Ofrenda | Materiales vanilla o Slimefun configurados para cada ritual. |
| Activacion | Coste por uso en poderes de alto impacto. |
| Tesoro del Olimpo | Fraccion limitada para eventos, bosses y recompensas. |
| Drenaje | Fraccion retirada de circulacion para controlar inflacion. |

Hefesto parte con valores piloto configurables: 1.200 para Forja Viva, 2.800 para
Pulso de Red y 4.500 para Ojo de Mena; su mantenimiento semanal inicial es 1.500.
El staff debe ajustar esos valores luego de la primera semana observando ingresos,
balances y el archivo de auditoria.

## Panteon

El proyecto contempla olimpicos y dioses secundarios: Zeus, Hera, Poseidon,
Demeter, Atenea, Apolo, Artemisa, Ares, Afrodita, Hefesto, Hermes, Hestia, Hades,
Persefone, Hecate, Dionisio, Eros, Nike, Nemesis, Morfeo, Helios, Selene y Tique.

Tambien incorpora a los doce Titanes del canon de Urano y Gea: Oceano, Ceo, Crio,
Hiperion, Japeto, Cronos, Tea, Rea, Temis, Mnemosine, Febe y Tetis. Consulta
[el canon jugable](docs/PANTEON.md) para sus ramas, limites y fuentes.

Todo el panteon inicial dispone de una rama base de tres nodos con costos,
restricciones y controles comunes. Las subramas especializadas e integraciones de
alto impacto se publican uno por uno, con pruebas y balance antes de avanzar.

El catalogo inicial define tres bendiciones por patron: una pasiva, una activa
con cooldown y una postura temporal. La carga final del jugador se limita a dos
pasivas, dos activas y una postura; tener un poder desbloqueado no significa que
pueda permanecer activo al mismo tiempo que todos los demas.

## Reglas de progreso

- Solo puede haber un dios activo por jugador.
- Renunciar elimina el progreso del dios actual y activa 48 horas de espera.
- El nuevo dios siempre comienza desde cero.
- Las bendiciones suspendidas por falta de pago conservan progreso durante la gracia.
- Nunca se venden poderes de progresion o combate mediante Tebex.

## Modos de combate

En PvP normal las bendiciones divinas permanecen desactivadas. `PvPDivino` sera una
arena separada. Sus poderes son de combate y el progreso se obtiene jugando,
nunca comprandolo con dinero.

## Seguridad y protecciones

Los poderes no pueden romper, usar, abrir ni atravesar territorios ajenos. Las
integraciones Slimefun se habilitan por listas explicitas: una habilidad no obtiene
acceso a una maquina, receta o red por existir; debe estar autorizada en configuracion.

`Pulso de Red` añade energia limitada a maquinas Slimefun expresamente permitidas
en `config.yml`, dentro del alcance y autorizacion del jugador. La lista inicial se
limita a la Mass Fabricator y al UU Crafter de LiteXpansion: consumidores de 16.666
y 50.000 J/s, respectivamente. No energiza generadores, baterias ni maquinas de la
linea Infinity/Cheat. `Ojo de Mena` marca
en el cliente una cantidad limitada de minerales durante ocho segundos, solo en
chunks cargados y donde WorldGuard permite interactuar. Ninguno modifica bloques,
inventarios ni protecciones.

## Hefesto: uso actual

1. Selecciona a Hefesto en `/dioses`.
2. Compra el primer nodo desde el menu o con `/dioses desbloquear hephaestus.forja_viva`.
3. Equipa una bendicion con el menu o `/dioses equipar <id>`.
4. Activa los poderes con `/dioses usar hephaestus.pulso_de_red` o
   `/dioses usar hephaestus.ojo_de_mena`.

## Códice y feedback

`/dioses libro` entrega el **Códice Divino**. El libro no conserva permisos ni
progreso: solamente explica la senda activa del jugador y puede pedirse de nuevo
sin riesgo. Para consultar cualquier nodo puntual existe `/dioses info <id>`.

Las activaciones muestran duración y recarga en la barra de acción. Cada familia
divina tiene partículas, color y sonido propios; Hefesto además muestra la energía
que logró inyectar en cada pulso. El menú del panteón deja visible el tipo del nodo,
su nivel, coste, prerrequisitos, duración y recarga antes de comprarlo.

## Árbol inicial

Cada patron empieza con tres nodos encadenados: una **pasiva equipada**, una
**activa con recarga** y una **postura temporal**. Los costes iniciales son 1.200,
2.800 y 4.500 Dragmas. El catálogo tiene 23 dioses olímpicos y menores, más 12
titanes; sus ramas iniciales suman 105 nodos documentados en
[`docs/PANTEON.md`](docs/PANTEON.md). Las expansiones se implementan patron por
patron con mecánicas reales, no como nodos de relleno.

La referencia jugable para la comunidad se publica en
`https://web.drakescraft.cl/dioses.html`. Al cambiar `SkillCatalog`, actualiza la
página pública en el mismo cambio para que el juego y su documentación no diverjan.

Las compras pasan por Vault, quedan en `plugins/DiosesDrakes/audit/` y se recuperan
por identificador si una operacion debe reintentarse. El mantenimiento se revisa al
conectar y cada cinco minutos; tras las 24 horas de gracia, las bendiciones se
suspenden sin borrar progreso hasta el siguiente pago exitoso.

## Desarrollo

Requisitos: Java 21 y un servidor Paper/Purpur compatible con 1.21.1.

```powershell
mvn clean package
```

El JAR se genera en `target/`. Las integraciones externas son opcionales en el
arranque y se activaran solo cuando sus adaptadores esten listos.

La economia viene activada para Hefesto. Si se requiere una puesta en marcha sin
cobros, define `economy.enabled: false`; el plugin conserva perfiles pero niega
habilidades con ofrenda y no ejecuta mantenimiento.

Consulta [la arquitectura](docs/ARCHITECTURE.md) para responsabilidades, reglas de
seguridad y plan de lanzamiento.
