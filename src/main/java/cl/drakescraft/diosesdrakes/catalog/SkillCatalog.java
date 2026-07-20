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

/** Initial data-driven catalog. Effects are activated only after their adapters are implemented. */
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
        return List.copyOf(skills);
    }

    private static void add(List<SkillDefinition> skills, GodId god, String suffix, SkillType type,
                            String description, int cooldown, int duration, int tier) {
        add(skills, god, suffix, type, description, cooldown, duration, tier, 0, List.of());
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
}
