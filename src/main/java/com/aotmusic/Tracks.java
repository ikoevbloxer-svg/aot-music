package com.aotmusic;

import java.util.ArrayList;
import java.util.List;

/** Все треки. Фоновые крутятся сами, командные включаются через /aotmusic play. */
public final class Tracks {
    public static final List<Track> ALL = List.of(
            // ---- фоновые: крутятся по очереди с паузами, как ванильная музыка
            new Track("call_your_name", "Call Your Name (Instrumental)", 5218, true),
            new Track("aots3_pf2", "AoTs3 PF2", 4106, true),
            new Track("eye_water", "Eye Water", 3652, true),
            new Track("apple_seed_instrumental", "Apple Seed (Instrumental)", 3310, true),
            // ---- командные: только по /aotmusic play
            new Track("t_k_0n_t_tn", "t_k_0N_t_tn", 5161, false),
            new Track("apetitan", "APETITAN", 6655, false),
            new Track("before_lights_out", "Before Lights Out", 3535, false),
            new Track("baukl_tze", "Baukl_tze", 4748, false),
            new Track("you_see_biggirl", "You See BIGGIRL", 7208, false),
            new Track("apple_seed_bertholdt", "Apple Seed (Bertholdt)", 3294, false)
    );

    public static Track byId(String id) {
        for (Track t : ALL) if (t.id().equals(id)) return t;
        return null;
    }

    public static List<Track> background() {
        List<Track> out = new ArrayList<>();
        for (Track t : ALL) if (t.background()) out.add(t);
        return out;
    }

    public static List<String> commandIds() {
        List<String> out = new ArrayList<>();
        for (Track t : ALL) if (!t.background()) out.add(t.id());
        return out;
    }

    private Tracks() {}
}
