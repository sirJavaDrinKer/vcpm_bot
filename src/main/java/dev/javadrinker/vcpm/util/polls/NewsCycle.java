package dev.javadrinker.vcpm.util.polls;

import dev.javadrinker.vcpm.util.data.NewsDataUtil;
import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;

public class NewsCycle {
    public static void checkPostLatestArticle(Guild guild) throws IOException, InterruptedException {
        if (!ServerDataUtil.getNewsEnabled(guild.getId())) { return; }
        ServerDataUtil.ServerData serverData = ServerDataUtil.getOrCreateServer(guild.getId());

        NewsDataUtil.Article article = NewsDataUtil.getLatestArticle();

        boolean articleNull = article == null;
        if (articleNull) {
            return;
        }

        boolean articleSame = article.getArticleId().equals(serverData.latest_news_id);
        if (articleSame) {
            return;
        }

        serverData.latest_news_id = NewsDataUtil.getLatestArticle().getArticleId();
        ServerDataUtil.save();

        if (serverData.news_channel==null) { return; }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(article.title);
        eb.setDescription("\""+article.description+"\"");
        eb.setUrl(article.url_path);
        eb.setFooter(article.author+" - "+article.date);

        guild.getTextChannelById(serverData.news_channel).sendMessageEmbeds(eb.build()).queue();
    }
}
