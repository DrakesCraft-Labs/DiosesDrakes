package cl.drakescraft.diosesdrakes.catalog;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.model.SkillType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Data-driven pantheon. Every patron has a ten-node path with real effect families. */
public final class SkillCatalog {
    private static final Map<String, SkillDefinition> BY_ID = build().stream()
            .collect(Collectors.toUnmodifiableMap(SkillDefinition::id, Function.identity()));

    private SkillCatalog() {
    }

    public static Optional<SkillDefinition> find(String id) {
        return Optional.ofNullable(BY_ID.get(id.toLowerCase(Locale.ROOT)));
    }

    public static Collection<SkillDefinition> forGod(GodId god) {
        return BY_ID.values().stream().filter(skill -> skill.god() == god)
                .sorted(Comparator.comparingInt(SkillDefinition::tier)).toList();
    }

    public static Collection<SkillDefinition> all() {
        return BY_ID.values();
    }

    private static List<SkillDefinition> build() {
        List<SkillDefinition> skills = new ArrayList<>();
        add(skills, GodId.HEPHAESTUS, "forja_viva", SkillType.PASSIVE, "Mejora la experiencia de fundicion autorizada.", 0, 0, 1, 1200, List.of());
        add(skills, GodId.HEPHAESTUS, "pulso_de_red", SkillType.STANCE, "Entrega energia limitada a maquinas Slimefun permitidas dentro de tu proteccion.", 45, 30, 2, 2800, List.of("hephaestus.forja_viva"));
        add(skills, GodId.HEPHAESTUS, "ojo_de_mena", SkillType.ACTIVE, "Marca minerales naturales por unos segundos sin alterar el mundo ni revelar protecciones ajenas.", 300, 8, 3, 4500, List.of("hephaestus.pulso_de_red"));
        add(skills, GodId.ZEUS, "chispa_regia", SkillType.PASSIVE, "Mitiga dano de tormenta y rayos.", 0, 0, 1);
        add(skills, GodId.ZEUS, "paso_del_rayo", SkillType.ACTIVE, "Impulso corto fuera de combate.", 90, 2, 2);
        add(skills, GodId.ZEUS, "barometro", SkillType.STANCE, "Informa cambios meteorologicos cercanos.", 60, 20, 3);
        add(skills, GodId.HERA, "velo_del_hogar", SkillType.PASSIVE, "Reduce dano ambiental dentro de tu territorio.", 0, 0, 1);
        add(skills, GodId.HERA, "juramento", SkillType.ACTIVE, "Marca temporalmente a un aliado para soporte.", 180, 20, 2);
        add(skills, GodId.HERA, "guardia_nupcial", SkillType.STANCE, "Refuerza defensa de un grupo pequeno.", 120, 30, 3);
        add(skills, GodId.POSEIDON, "pulmon_marino", SkillType.PASSIVE, "Mejora respiracion y nado bajo el agua.", 0, 0, 1);
        add(skills, GodId.POSEIDON, "corriente", SkillType.ACTIVE, "Impulso acuatico limitado.", 75, 3, 2);
        add(skills, GodId.POSEIDON, "marea_serena", SkillType.STANCE, "Mejora pesca y exploracion marina autorizada.", 90, 25, 3);
        add(skills, GodId.DEMETER, "mano_fertil", SkillType.PASSIVE, "Aumenta experiencia de agricultura.", 0, 0, 1);
        add(skills, GodId.DEMETER, "cosecha_ritual", SkillType.ACTIVE, "Replanta cultivos maduros del propio jugador.", 120, 1, 2);
        add(skills, GodId.DEMETER, "estacion_dorada", SkillType.STANCE, "Bonifica granjas permitidas durante una ventana corta.", 180, 45, 3);
        add(skills, GodId.ATHENA, "mente_tactica", SkillType.PASSIVE, "Bonifica experiencia de conocimiento configurado.", 0, 0, 1);
        add(skills, GodId.ATHENA, "analisis", SkillType.ACTIVE, "Explica un objeto o maquina permitida.", 30, 1, 2);
        add(skills, GodId.ATHENA, "consejo", SkillType.STANCE, "Muestra objetivos y riesgos contextuales.", 90, 25, 3);
        add(skills, GodId.APOLLO, "luz_sagrada", SkillType.PASSIVE, "Reduce efectos de oscuridad y mejora vision.", 0, 0, 1);
        add(skills, GodId.APOLLO, "destello", SkillType.ACTIVE, "Ilumina una zona sin modificar bloques.", 75, 8, 2);
        add(skills, GodId.APOLLO, "oraculo", SkillType.STANCE, "Entrega pistas de exploracion configuradas.", 150, 25, 3);
        add(skills, GodId.ARTEMIS, "paso_silvestre", SkillType.PASSIVE, "Mejora movilidad en naturaleza.", 0, 0, 1);
        add(skills, GodId.ARTEMIS, "rastro", SkillType.ACTIVE, "Rastrea criaturas sin revelar jugadores.", 100, 12, 2);
        add(skills, GodId.ARTEMIS, "luna_cazadora", SkillType.STANCE, "Bonifica caza PvE nocturna.", 150, 30, 3);
        add(skills, GodId.ARES, "temple", SkillType.PASSIVE, "Reduce retroceso en PvE.", 0, 0, 1);
        add(skills, GodId.ARES, "grito_belico", SkillType.ACTIVE, "Buff breve contra criaturas, nunca en PvP normal.", 120, 12, 2);
        add(skills, GodId.ARES, "vanguardia", SkillType.STANCE, "Postura exclusiva para PvPDivino.", 180, 35, 3);
        add(skills, GodId.APHRODITE, "encanto", SkillType.PASSIVE, "Mejora intercambios configurados con aldeanos.", 0, 0, 1);
        add(skills, GodId.APHRODITE, "serenidad", SkillType.ACTIVE, "Reduce agresion de criaturas cercanas.", 120, 10, 2);
        add(skills, GodId.APHRODITE, "pacto", SkillType.STANCE, "Aura social para grupos consentidos.", 180, 25, 3);
        add(skills, GodId.HERMES, "pies_alados", SkillType.PASSIVE, "Pequena mejora de velocidad fuera de combate.", 0, 0, 1);
        add(skills, GodId.HERMES, "ascenso_de_icaro", SkillType.ACTIVE, "Levitacion breve con caida lenta y limites regionales.", 240, 4, 2);
        add(skills, GodId.HERMES, "ruta_mercante", SkillType.STANCE, "Reduce costos de viaje configurados.", 180, 30, 3);
        add(skills, GodId.HESTIA, "brasa_eterea", SkillType.PASSIVE, "Resistencia menor al fuego en tu hogar.", 0, 0, 1);
        add(skills, GodId.HESTIA, "fogata", SkillType.ACTIVE, "Crea luz temporal sin fuego real.", 90, 30, 2);
        add(skills, GodId.HESTIA, "refugio", SkillType.STANCE, "Aura de descanso para miembros autorizados.", 180, 40, 3);
        add(skills, GodId.HADES, "guia_estigia", SkillType.PASSIVE, "Mejora recuperacion ligada a tumbas propias.", 0, 0, 1);
        add(skills, GodId.HADES, "velo_de_ceniza", SkillType.ACTIVE, "Resistencia corta en el Nether.", 150, 15, 2);
        add(skills, GodId.HADES, "pacto_funebre", SkillType.STANCE, "Protege la tumba propia bajo reglas AxGraves.", 240, 30, 3);
        add(skills, GodId.PERSEPHONE, "semilla_sombra", SkillType.PASSIVE, "Bonifica cultivos del Nether permitidos.", 0, 0, 1);
        add(skills, GodId.PERSEPHONE, "retorno", SkillType.ACTIVE, "Reduce perdida de hambre tras dimension peligrosa.", 120, 8, 2);
        add(skills, GodId.PERSEPHONE, "ciclo", SkillType.STANCE, "Alterna bonificaciones de cultivo y Nether.", 180, 30, 3);
        add(skills, GodId.HECATE, "runas", SkillType.PASSIVE, "Mejora lectura de rituales autorizados.", 0, 0, 1);
        add(skills, GodId.HECATE, "umbral", SkillType.ACTIVE, "Revela rutas seguras sin atravesar protecciones.", 150, 10, 2);
        add(skills, GodId.HECATE, "triple_luna", SkillType.STANCE, "Alterna una postura ritual limitada.", 180, 30, 3);
        add(skills, GodId.DIONYSUS, "vendimia", SkillType.PASSIVE, "Mejora preparacion de bebidas configuradas.", 0, 0, 1);
        add(skills, GodId.DIONYSUS, "festin", SkillType.ACTIVE, "Comparte saciedad limitada con aliados.", 150, 12, 2);
        add(skills, GodId.DIONYSUS, "extasis", SkillType.STANCE, "Aura de evento sin efectos de combate.", 180, 30, 3);
        add(skills, GodId.EROS, "empatia", SkillType.PASSIVE, "Bonifica cooperacion de grupo consentida.", 0, 0, 1);
        add(skills, GodId.EROS, "lazo", SkillType.ACTIVE, "Conecta temporalmente estados de apoyo aliados.", 150, 15, 2);
        add(skills, GodId.EROS, "corazon", SkillType.STANCE, "Postura social que nunca afecta PvP normal.", 180, 30, 3);
        add(skills, GodId.NIKE, "impulso", SkillType.PASSIVE, "Aumenta experiencia por objetivos completados.", 0, 0, 1);
        add(skills, GodId.NIKE, "remate", SkillType.ACTIVE, "Bonificacion breve contra bosses configurados.", 150, 10, 2);
        add(skills, GodId.NIKE, "corona", SkillType.STANCE, "Postura de desafio para eventos globales.", 180, 30, 3);
        add(skills, GodId.NEMESIS, "equilibrio", SkillType.PASSIVE, "Reduce ventajas extremas contra criaturas.", 0, 0, 1);
        add(skills, GodId.NEMESIS, "juicio", SkillType.ACTIVE, "Debilita temporalmente un boss configurado.", 180, 8, 2);
        add(skills, GodId.NEMESIS, "balanza", SkillType.STANCE, "Postura de riesgo y recompensa PvE.", 210, 30, 3);
        add(skills, GodId.MORPHEUS, "descanso", SkillType.PASSIVE, "Mejora recuperacion al dormir.", 0, 0, 1);
        add(skills, GodId.MORPHEUS, "sueno_lucido", SkillType.ACTIVE, "Muestra una pista de exploracion personal.", 180, 10, 2);
        add(skills, GodId.MORPHEUS, "niebla", SkillType.STANCE, "Reduce deteccion de criaturas fuera de combate.", 210, 30, 3);
        add(skills, GodId.HELIOS, "calor", SkillType.PASSIVE, "Mejora vision diurna.", 0, 0, 1);
        add(skills, GodId.HELIOS, "rayo_solar", SkillType.ACTIVE, "Destello PvE limitado y documentado.", 180, 6, 2);
        add(skills, GodId.HELIOS, "mediodia", SkillType.STANCE, "Postura de exploracion bajo el sol.", 210, 30, 3);
        add(skills, GodId.SELENE, "guia_lunar", SkillType.PASSIVE, "Mejora vision y sigilo nocturno.", 0, 0, 1);
        add(skills, GodId.SELENE, "salto_lunar", SkillType.ACTIVE, "Caida lenta corta fuera de combate.", 180, 6, 2);
        add(skills, GodId.SELENE, "plenilunio", SkillType.STANCE, "Postura nocturna para exploracion segura.", 210, 30, 3);
        add(skills, GodId.TYCHE, "fortuna_menor", SkillType.PASSIVE, "Pequena mejora de recompensa configurada.", 0, 0, 1);
        add(skills, GodId.TYCHE, "segunda_oportunidad", SkillType.ACTIVE, "Reintento limitado de recompensa no critica.", 240, 1, 2);
        add(skills, GodId.TYCHE, "rueda", SkillType.STANCE, "Postura de evento con recompensa acotada.", 240, 30, 3);
        add(skills, GodId.OCEANUS, "corriente_primordial", SkillType.PASSIVE, "Mejora el desplazamiento y la respiracion en agua.", 0, 0, 1);
        add(skills, GodId.OCEANUS, "anillo_del_mundo", SkillType.ACTIVE, "Crea una corriente defensiva breve alrededor del jugador.", 150, 8, 2);
        add(skills, GodId.OCEANUS, "rio_celeste", SkillType.STANCE, "Postura de exploracion marina con recuperacion limitada.", 210, 30, 3);
        add(skills, GodId.COEUS, "eje_celeste", SkillType.PASSIVE, "Mantiene la orientacion y la vision en exploracion nocturna.", 0, 0, 1);
        add(skills, GodId.COEUS, "consulta_del_polo", SkillType.ACTIVE, "Entrega coordenadas y direccion segura sin revelar jugadores.", 120, 1, 2);
        add(skills, GodId.COEUS, "cartografia_astral", SkillType.STANCE, "Postura de navegacion que reduce riesgos de exploracion.", 180, 30, 3);
        add(skills, GodId.CRIUS, "pastor_de_estrellas", SkillType.PASSIVE, "Mejora la movilidad durante la noche.", 0, 0, 1);
        add(skills, GodId.CRIUS, "constelacion", SkillType.ACTIVE, "Concede un salto lunar temporal fuera de combate.", 150, 8, 2);
        add(skills, GodId.CRIUS, "estacion", SkillType.STANCE, "Postura nocturna de resistencia para viajes largos.", 210, 30, 3);
        add(skills, GodId.HYPERION, "vigilia_del_sol", SkillType.PASSIVE, "Mejora la vision y recuperacion durante el dia.", 0, 0, 1);
        add(skills, GodId.HYPERION, "alba", SkillType.ACTIVE, "Disipa oscuridad y aplica brillo temporal al invocador.", 150, 10, 2);
        add(skills, GodId.HYPERION, "cenit", SkillType.STANCE, "Postura solar de resistencia ambiental limitada.", 210, 30, 3);
        add(skills, GodId.IAPETUS, "temple_mortal", SkillType.PASSIVE, "Reduce dano ambiental moderado fuera de PvP.", 0, 0, 1);
        add(skills, GodId.IAPETUS, "prevision", SkillType.ACTIVE, "Entrega caida lenta y resistencia breve ante un viaje riesgoso.", 180, 8, 2);
        add(skills, GodId.IAPETUS, "legado", SkillType.STANCE, "Postura de supervivencia que mejora la recuperacion propia.", 240, 30, 3);
        add(skills, GodId.CRONUS, "ritmo_antiguo", SkillType.PASSIVE, "Acelera la recuperacion natural sin alterar el tiempo del mundo.", 0, 0, 1);
        add(skills, GodId.CRONUS, "instante_robado", SkillType.ACTIVE, "Otorga velocidad corta, con caida lenta y sin teletransporte.", 210, 6, 2);
        add(skills, GodId.CRONUS, "edad_dorada", SkillType.STANCE, "Postura de descanso que reduce desgaste ambiental.", 300, 30, 3);
        add(skills, GodId.THEIA, "vista_divina", SkillType.PASSIVE, "Reduce ceguera y mejora vision en entornos oscuros.", 0, 0, 1);
        add(skills, GodId.THEIA, "refraccion", SkillType.ACTIVE, "Marca una ruta inmediata con luz de cliente temporal.", 150, 8, 2);
        add(skills, GodId.THEIA, "resplandor", SkillType.STANCE, "Postura de vision y resistencia luminosa controlada.", 210, 30, 3);
        add(skills, GodId.RHEA, "madre_salvaje", SkillType.PASSIVE, "Mejora la regeneracion fuera de combate.", 0, 0, 1);
        add(skills, GodId.RHEA, "amparo", SkillType.ACTIVE, "Entrega absorcion breve sin afectar a otros jugadores.", 180, 10, 2);
        add(skills, GodId.RHEA, "montana_viva", SkillType.STANCE, "Postura terrestre de resistencia y estabilidad.", 240, 30, 3);
        add(skills, GodId.THEMIS, "orden_sagrado", SkillType.PASSIVE, "Reduce efectos negativos de corta duracion.", 0, 0, 1);
        add(skills, GodId.THEMIS, "decreto", SkillType.ACTIVE, "Limpia un efecto negativo permitido del propio jugador.", 180, 1, 2);
        add(skills, GodId.THEMIS, "balanza_cosmica", SkillType.STANCE, "Postura defensiva que no altera PvP normal.", 240, 30, 3);
        add(skills, GodId.MNEMOSYNE, "memoria_perenne", SkillType.PASSIVE, "Mejora la conservacion de experiencia mediante reglas configuradas.", 0, 0, 1);
        add(skills, GodId.MNEMOSYNE, "recuerdo", SkillType.ACTIVE, "Muestra informacion de la ubicacion actual sin leer inventarios ajenos.", 150, 1, 2);
        add(skills, GodId.MNEMOSYNE, "musa", SkillType.STANCE, "Postura de concentracion con recuperacion de mana configurada.", 210, 30, 3);
        add(skills, GodId.PHOEBE, "oraculo_antiguo", SkillType.PASSIVE, "Mejora vision nocturna y resistencia a oscuridad.", 0, 0, 1);
        add(skills, GodId.PHOEBE, "augurio", SkillType.ACTIVE, "Entrega una pista de exploracion sin revelar recursos ni jugadores.", 150, 1, 2);
        add(skills, GodId.PHOEBE, "santuario", SkillType.STANCE, "Postura lunar de proteccion personal fuera de PvP.", 210, 30, 3);
        add(skills, GodId.TETHYS, "fuente_nutricia", SkillType.PASSIVE, "Mejora respiracion y recuperacion al estar en agua.", 0, 0, 1);
        add(skills, GodId.TETHYS, "manantial", SkillType.ACTIVE, "Restaura hambre limitada y limpia efectos de calor propios.", 180, 1, 2);
        add(skills, GodId.TETHYS, "cauce", SkillType.STANCE, "Postura acuatica de movilidad y resistencia ambiental.", 240, 30, 3);
        expandEveryBranch(skills);
        extendCombatMastery(skills);
        return List.copyOf(skills);
    }

    /** Adds the seven ascension nodes shared structurally by all patrons, with themed identities. */
    private static void expandEveryBranch(List<SkillDefinition> skills) {
        for (GodId god : GodId.values()) {
            PathTheme theme = themeFor(god);
            add(skills, god, "ascension_" + theme.passive(), SkillType.PASSIVE,
                    "Legado " + theme.label() + ": una bendicion mayor equipada que refuerza tu afinidad.", 0, 0, 4);
            add(skills, god, "ascension_" + theme.strike(), SkillType.ACTIVE,
                    "Descarga " + theme.label() + " sobre criaturas cercanas. Nunca dana jugadores fuera de PvPDivino.", 150, 1, 5);
            add(skills, god, "ascension_" + theme.flight(), SkillType.ACTIVE,
                    mobilityDescription(theme, god), 240, 10, 6);
            add(skills, god, "ascension_" + theme.domain(), SkillType.STANCE,
                    "Dominio " + theme.label() + ": altera solo tu experiencia local y respeta claims y protecciones.", 300, 20, 7);
            add(skills, god, "ascension_" + theme.execution(), SkillType.ACTIVE,
                    "Veredicto " + theme.label() + ": golpe de 100 de dano contra una criatura, nunca contra jugadores.", 480, 1, 8);
            add(skills, god, "ascension_" + theme.avatar(), SkillType.STANCE,
                    "Avatar " + theme.label() + ": forma colosal temporal, resistencia y presencia visible.", 600, 18, 9);
            add(skills, god, "ascension_" + theme.capstone(), SkillType.PASSIVE,
                    "Corona " + theme.label() + ": pasiva final que potencia tu especialidad mientras este equipada.", 0, 0, 10);
        }
    }

    /** Adds a shared combat grammar while retaining a distinct elemental presentation for every patron. */
    private static void extendCombatMastery(List<SkillDefinition> skills) {
        for (GodId god : GodId.values()) {
            PathTheme theme = themeFor(god);
            add(skills, god, "combate_" + theme.strike() + "_punos", SkillType.ACTIVE,
                    "Puños " + theme.label() + ": requiere mano vacia y remata una criatura con un impacto breve.", 70, 1, 11);
            add(skills, god, "combate_" + theme.execution() + "_arma", SkillType.ACTIVE,
                    "Arma " + theme.label() + ": requiere espada, hacha, maza o lanza y ejecuta una tecnica PvE de alto impacto.", 150, 1, 12);
            add(skills, god, "combate_" + theme.flight() + "_carrera", SkillType.ACTIVE,
                    "Carrera " + theme.label() + ": embestida corta con velocidad y caida segura, sin teletransporte.", 110, 5, 13);
            add(skills, god, "combate_" + theme.domain() + "_guardia", SkillType.STANCE,
                    "Guardia " + theme.label() + ": absorbe parte del siguiente castigo de una criatura y devuelve un impacto visual.", 180, 15, 14);
            add(skills, god, "combate_" + theme.capstone() + "_maestria", SkillType.PASSIVE,
                    "Maestria " + theme.label() + ": pasiva final para sostener tu build de combate equipada.", 0, 0, 15);
        }
    }

    private static String mobilityDescription(PathTheme theme, GodId god) {
        return switch (mobilityFor(god)) {
            case FLIGHT -> "Ascenso " + theme.label() + ": vuelo breve, impulso y caida segura fuera de combate.";
            case WATER -> "Ascenso " + theme.label() + ": corriente marina, respiracion y desplazamiento acuatico breve.";
            case SHADOW -> "Ascenso " + theme.label() + ": deslizamiento sigiloso con velocidad y caida segura.";
            case FORGE -> "Ascenso " + theme.label() + ": carrera entre brasas con resistencia al fuego y velocidad.";
            default -> "Ascenso " + theme.label() + ": salto de combate y velocidad breve, sin vuelo ni teletransporte.";
        };
    }

    private static Mobility mobilityFor(GodId god) {
        return switch (god) {
            case HERMES, ARTEMIS, SELENE, CRIUS -> Mobility.FLIGHT;
            case POSEIDON, OCEANUS, TETHYS -> Mobility.WATER;
            case HADES, HECATE, MORPHEUS -> Mobility.SHADOW;
            case HEPHAESTUS, HESTIA, IAPETUS, CRONUS -> Mobility.FORGE;
            default -> Mobility.DASH;
        };
    }

    /** Maps mythology to the effect family used by the runtime executor. */
    private static PathTheme themeFor(GodId god) {
        return switch (god) {
            case ZEUS, ARES, NIKE, NEMESIS -> new PathTheme("del trueno", "corona_tonante", "lanza_del_cielo", "paso_del_rayo", "tempestad_personal", "veredicto_del_olimpo", "avatar_tonante", "trono_electrico");
            case POSEIDON, OCEANUS, TETHYS -> new PathTheme("de la marea", "pulmon_abismal", "tridente_de_ola", "salto_de_marea", "reino_de_lluvia", "veredicto_abismal", "avatar_del_mar", "corona_de_coral");
            case DEMETER, PERSEPHONE, DIONYSUS, RHEA -> new PathTheme("del florecimiento", "raiz_perenne", "espina_viviente", "salto_de_brote", "jardin_sagrado", "veredicto_de_raiz", "avatar_verdante", "corona_de_la_cosecha");
            case HERMES, ARTEMIS, SELENE, CRIUS -> new PathTheme("del viento", "sendero_alado", "flecha_de_viento", "vuelo_del_mensajero", "cielo_personal", "veredicto_del_cazador", "avatar_alado", "corona_del_viajero");
            case APOLLO, HELIOS, HYPERION, THEIA, PHOEBE -> new PathTheme("de la luz", "vista_inmortal", "rayo_solar", "ascenso_del_amanecer", "halo_personal", "veredicto_solar", "avatar_radiante", "corona_del_mediodia");
            case HADES, HECATE, MORPHEUS -> new PathTheme("de la sombra", "velo_perenne", "lanza_estigia", "ascenso_espectral", "niebla_personal", "veredicto_estigio", "avatar_del_inframundo", "corona_de_ceniza");
            case ATHENA, HERA, COEUS, THEMIS, MNEMOSYNE -> new PathTheme("del juicio", "mente_inmortal", "decreto_divino", "paso_tactico", "santuario_personal", "veredicto_de_la_ley", "avatar_de_marfil", "corona_del_consejo");
            case HEPHAESTUS, HESTIA, IAPETUS, CRONUS -> new PathTheme("de la forja", "corazon_de_brasa", "martillo_celeste", "salto_de_ascua", "forja_personal", "veredicto_de_acero", "avatar_de_bronce", "corona_del_yunque");
            case APHRODITE, EROS, TYCHE -> new PathTheme("del encanto", "aura_favorable", "onda_de_gracia", "paso_afortunado", "jardin_de_gracia", "veredicto_del_destino", "avatar_de_rosa", "corona_de_la_fortuna");
        };
    }

    private static void add(List<SkillDefinition> skills, GodId god, String suffix, SkillType type,
                            String description, int cooldown, int duration, int tier) {
        List<String> prerequisites = skills.stream()
                .filter(skill -> skill.god() == god && skill.tier() == tier - 1)
                .reduce((first, second) -> second)
                .map(SkillDefinition::id)
                .map(List::of)
                .orElseGet(List::of);
        add(skills, god, suffix, type, description, cooldown, duration, tier, defaultCost(tier), prerequisites);
    }

    private static void add(List<SkillDefinition> skills, GodId god, String suffix, SkillType type,
                            String description, int cooldown, int duration, int tier, double unlockCost,
                            List<String> prerequisites) {
        skills.add(new SkillDefinition(god.name().toLowerCase(Locale.ROOT) + "." + suffix,
                god, title(suffix), type, description, cooldown, duration, tier, unlockCost, prerequisites));
    }

    private static String title(String id) {
        return id.replace('_', ' ');
    }

    private static double defaultCost(int tier) {
        return switch (tier) {
            case 1 -> 10_000;
            case 2 -> 50_000;
            case 3 -> 150_000;
            case 4 -> 400_000;
            case 5 -> 900_000;
            case 6 -> 2_000_000;
            case 7 -> 4_500_000;
            case 8 -> 9_000_000;
            case 9 -> 18_000_000;
            case 10 -> 36_000_000;
            case 11 -> 24_000_000;
            case 12 -> 38_000_000;
            case 13 -> 55_000_000;
            case 14 -> 75_000_000;
            default -> 100_000_000;
        };
    }

    private enum Mobility { FLIGHT, WATER, SHADOW, FORGE, DASH }

    private record PathTheme(String label, String passive, String strike, String flight, String domain,
                             String execution, String avatar, String capstone) {
    }
}
