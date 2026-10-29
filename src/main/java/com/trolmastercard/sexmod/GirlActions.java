package com.trolmastercard.sexmod;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * Per-character action menus, with the item prices recovered from the 1.12.2 source.
 * Each scene is a chain of clips: one-shot clips advance on their own, looping clips wait for
 * the player's "next stage" key, and the scene ends after its last one-shot clip.
 */
public final class GirlActions {
    public static final String STRIP = "strip";
    public static final String DRESS = "dress";

    public record Action(String id, @Nullable Item cost, int count, boolean partner, boolean stripFirst, List<String> stages) {
        public String translationKey() {
            return "action.sexmod." + this.id;
        }
    }

    private static final Map<GirlType, List<Action>> ACTIONS = new EnumMap<>(GirlType.class);

    static {
        add(GirlType.JENNY, scene("blowjob", Items.EMERALD, 3, true, "blowjobintro", "blowjobsuck", "blowjobthrust", "blowjobcum"));
        add(GirlType.JENNY, scene("boobjob", Items.ENDER_PEARL, 2, true, "paizuri_start", "paizuri_slow", "paizuri_fast", "paizuri_cum"));
        add(GirlType.JENNY, scene("doggy", Items.DIAMOND, 2, true, "doggystart", "doggyslow", "doggyfast_soft", "doggyfast_hard", "doggycum"));
        add(GirlType.JENNY, solo("wave", "wave"));

        add(GirlType.ELLIE, scene("missionary", null, 0, true, "missionary_start", "missionary_slow", "missionary_fast", "missionary_cum"));
        add(GirlType.ELLIE, scene("cowgirl", null, 0, true, "cowgirlstart", "cowgirlslow2", "cowgirlfast", "cowgirlcum"));
        add(GirlType.ELLIE, scene("carry", null, 0, true, "carry_intro", "carry_slow1", "carry_slow2", "carry_fast", "carry_cum"));
        add(GirlType.ELLIE, scene("hug", null, 0, false, "hug"));

        add(GirlType.BIA, scene("anal", null, 0, true, "anal_prepare", "anal_start", "anal_slow", "anal_fast", "anal_cum"));
        add(GirlType.BIA, scene("prone_doggy", null, 0, true, "prone_doggy_intro", "prone_doggy_insert", "prone_doggy_soft", "prone_doggy_hard1", "prone_doggy_cum"));
        add(GirlType.BIA, scene("headpat", null, 0, false, "headpat"));
        add(GirlType.BIA, solo("wave", "wave"));

        add(GirlType.BEE, scene("sex", null, 0, false, "sex_start", "sex_slow", "sex_fast", "sex_cum"));

        add(GirlType.LUNA, scene("sex", Items.COD, 3, false, "sitting_intro", "sitting_slow", "sitting_fast", "sitting_cum"));
        add(GirlType.LUNA, scene("touch_boobs", Items.SALMON, 2, false, "touch_boobs_intro", "touch_boobs_slow", "touch_boobs_fast", "touch_boobs_cum"));
        add(GirlType.LUNA, scene("headpat", null, 0, false, "head_pat"));

        add(GirlType.ALLIE, scene("deepthroat", null, 0, false, "deepthroat_prepare", "deepthroat_start", "deepthroat_slow", "deepthroat_fast", "deepthroat_cum"));
        add(GirlType.ALLIE, scene("reverse_cowgirl", null, 0, false, "reverse_cowgirl_start", "reverse_cowgirl_slow1", "reverse_cowgirl_fasts", "reverse_cowgirl_cum"));
        add(GirlType.ALLIE, solo("rich", "rich"));

        add(GirlType.SLIME, scene("doggy", null, 0, true, "doggystart", "doggyslow", "doggyfast", "doggycum"));
        add(GirlType.SLIME, scene("blowjob", null, 0, true, "blowjobsuck", "blowjobthrust", "blowjobcum"));

        add(GirlType.GALATH, scene("bed", null, 0, false, "bed_slow", "bed_fast", "bed_cum"));
        add(GirlType.GALATH, scene("corrupt", null, 0, false, "corrupt_intro", "corrupt_slow", "corrupt_hard", "corrupt_cum"));
        add(GirlType.GALATH, scene("licking", null, 0, false, "pussy_licking"));
        add(GirlType.GALATH, solo("masturbate", "masterbating_sitting", "masterbating_sitting_cum"));

        add(GirlType.MANGLELIE, scene("double", null, 0, false, "double_holding_slow", "double_holding_hard", "double_holding_cum"));
    }

    private GirlActions() {}

    private static Action scene(String id, @Nullable Item cost, int count, boolean stripFirst, String... stages) {
        return new Action(id, cost, count, true, stripFirst, List.of(stages));
    }

    private static Action solo(String id, String... stages) {
        return new Action(id, null, 0, false, false, List.of(stages));
    }

    private static void add(GirlType type, Action action) {
        ACTIONS.computeIfAbsent(type, ignored -> new ArrayList<>()).add(action);
    }

    /** The actions offered for a character; clips missing from the assets are dropped. */
    public static List<Action> forType(GirlType type) {
        AnimationIndex index = AnimationIndex.get(type);
        List<Action> result = new ArrayList<>();
        for (Action action : ACTIONS.getOrDefault(type, List.of())) {
            List<String> stages = action.stages().stream().filter(index::has).toList();
            if (!stages.isEmpty()) {
                result.add(new Action(action.id(), action.cost(), action.count(), action.partner(), action.stripFirst(), stages));
            }
        }
        return result;
    }

    public static @Nullable Action find(GirlType type, String id) {
        for (Action action : forType(type)) {
            if (action.id().equals(id)) return action;
        }
        return null;
    }
}
