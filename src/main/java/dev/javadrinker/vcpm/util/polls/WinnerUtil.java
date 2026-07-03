package dev.javadrinker.vcpm.util.polls;

import dev.javadrinker.vcpm.util.data.MatchDataUtil;
import dev.javadrinker.vcpm.util.data.TeamDataUtil;

import java.io.IOException;

public class WinnerUtil {
    protected static String getWinnerTeam(MatchDataUtil.PastMatch match) {
        int score1 = Integer.parseInt(match.score1);
        int score2 = Integer.parseInt(match.score2);
        if (score1 > score2) {
            return match.team1;
        } else if (score2 > score1) {
            return match.team2;
        } else {
            return "Draw";
        }
    }

    protected static String getWinnerFlag(MatchDataUtil.PastMatch match) throws IOException, InterruptedException {
        String team1 = match.team1;
        if (TeamDataUtil.getTeamByName(match.team1)==null || TeamDataUtil.getTeamByName(match.team2)==null) {
            return "https://placehold.co/500x500.png?text=?";
        }
        if (getWinnerTeam(match)==null) {
            return "https://placehold.co/500x500.png?text=?";
        }
        if (getWinnerTeam(match).equals(team1)) {
            return TeamDataUtil.getTeamByName(match.team1).img;
        } else {
            return TeamDataUtil.getTeamByName(match.team2).img;
        }
    }
}
