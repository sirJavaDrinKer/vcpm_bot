package dev.javadrinker.vcpm.util.polls;

import dev.javadrinker.vcpm.util.data.NewsDataUtil;
import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class NewsCycle {

    @Deprecated
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

        if (article.description.isEmpty()) {
            eb.setDescription("*No summary provided.*");
        } else {
            eb.setDescription("\""+article.description+"\"");
        }

        eb.setUrl(article.url_path);
        eb.setFooter(article.author+" - "+article.date);

        guild.getTextChannelById(serverData.news_channel).sendMessageEmbeds(eb.build()).queue();
    }

    public static void compareAndCleanArticleLists(Guild guild) throws IOException, InterruptedException {
        if (!ServerDataUtil.getNewsEnabled(guild.getId())) { return; }

        List<NewsDataUtil.Article> apiArticleList = NewsDataUtil.getArticles();
        List<String> serverArticleList = ServerDataUtil.getNewsIds(guild.getId());

        if (apiArticleList == null) { return; }
        if (apiArticleList.isEmpty()) { return; }

        // This is so that oldest articles are posted first, preventing chronological confusion.
        sortArticlesByOldest(apiArticleList);

        for (NewsDataUtil.Article article : apiArticleList) {
            if (serverArticleList.contains(article.getArticleId())) {
                continue;
            }

            postArticles(article, guild);
            serverArticleList.add(article.getArticleId());
        }

        serverArticleList.retainAll(
            apiArticleList.stream()
                .map(NewsDataUtil.Article::getArticleId)
                .toList()
        );

        ServerDataUtil.setNewsIds(guild.getId(), serverArticleList);
    }

    private static void postArticles(NewsDataUtil.Article article, Guild guild) throws IOException, InterruptedException {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(article.title);
        eb.setDescription("\""+article.description+"\"");
        eb.setUrl(article.url_path);
        eb.setFooter(article.author+" - "+article.date);

        ServerDataUtil.ServerData serverData = ServerDataUtil.getOrCreateServer(guild.getId());

        guild.getTextChannelById(serverData.news_channel).sendMessageEmbeds(eb.build()).queue();
    }


    private static void sortArticlesByOldest(List<NewsDataUtil.Article> articles) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

        articles.sort(Comparator.comparing(article ->
                LocalDate.parse(article.date, formatter)
        ));
    }
}
