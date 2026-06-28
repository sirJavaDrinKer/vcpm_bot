package dev.javadrinker.vcpm.util.polls;

import dev.javadrinker.vcpm.util.data.NewsDataUtil;
import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import dev.javadrinker.vcpm.util.scraper.NewsArticleScraper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.Components;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NewsCycle extends ListenerAdapter {

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



        guild.getTextChannelById(serverData.news_channel).sendMessageEmbeds(eb.build()).addComponents(
                ActionRow.of(
                        Button.primary("open-discussion_"+article.getArticleId(), "Open Discussion"),
                        Button.secondary("read-article_"+article.getArticleId(), "Read Full Article Here"))
        ).queue();
    }

    public void onButtonInteraction(ButtonInteractionEvent e) {
        if (e.getComponentId().startsWith("open-discussion_")) {
            openThread(e);
        } else if (e.getComponentId().startsWith("read-article_")) {
            postArticle(e);
        }


    }

    private void openThread(ButtonInteractionEvent e) {
        e.editButton(e.getButton().asDisabled()).queue();

        String threadTitle = "Discussion: " +e.getMessage().getEmbeds().get(0).getTitle();

        if (threadTitle.length()>100) {
            threadTitle = threadTitle.substring(0, 97)+"...";
        }

        e.getMessage().createThreadChannel(threadTitle).queue(threadChannel -> {
            threadChannel.addThreadMember(e.getUser()).queue();
            threadChannel.sendMessage("Discussion opened by "+e.getUser().getName()).queue();
        });
    }
    private void postArticle(ButtonInteractionEvent e) {
        String url = "https://www.vlr.gg/"+e.getComponentId().split("_")[1]+"/";
        ArrayList<String> articleSegments = NewsArticleScraper.getArticleSegments(url);

        if (articleSegments.isEmpty()) {
            e.reply("Failed to retrieve article content, you may click the title of the post to read on VLR.").setEphemeral(true).queue();
            return;
        }

        if (articleSegments.size()>10) {
            e.reply("Article is too long to post, you may click the title of the post to read on VLR.").setEphemeral(true).queue();
            return;
        }

        MessageEmbed[] eb = new MessageEmbed[articleSegments.size()];

        for (String segment : articleSegments) {
            int i = articleSegments.indexOf(segment);
            eb[i] = new EmbedBuilder().setDescription(segment).build();
        }

        e.replyEmbeds(Arrays.asList(eb)).setEphemeral(true).queue();

    }




    private static void sortArticlesByOldest(List<NewsDataUtil.Article> articles) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

        articles.sort(Comparator.comparing(article ->
                LocalDate.parse(article.date, formatter)
        ));
    }

    public List<MessageTopLevelComponent> getDisabledLayout(List<MessageTopLevelComponentUnion> components, String targetId) {
        List<MessageTopLevelComponent> updatedRows = new ArrayList<>();

        for (MessageTopLevelComponentUnion union : components) {
            if (union.getType() == Component.Type.ACTION_ROW) {
                ActionRow row = union.asActionRow();
                List<ActionRowChildComponent> items = new ArrayList<>(row.getComponents());
                boolean modified = false;

                for (int i = 0; i < items.size(); i++) {
                    ActionRowChildComponent item = items.get(i);

                    // If it's a button and matches our target ID
                    if (item instanceof Button button && targetId.equals(button.getCustomId())) {
                        items.set(i, button.asDisabled());
                        modified = true;
                    }
                }
                updatedRows.add(modified ? ActionRow.of(items) : row);
            } else {
                updatedRows.add(union);
            }
        }
        return updatedRows;
    }

}
