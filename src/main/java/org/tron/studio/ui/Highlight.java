package org.tron.studio.ui;

import javafx.concurrent.Task;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import org.tron.studio.MainApplication;
import org.tron.studio.ShareData;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

/**
 * Base of Highlighting.
 *
 * @author Toast kid
 */
public abstract class Highlight {

    /** Target CodeArea. */
    protected final CodeArea codeArea;

    /** ExecutorService. */
    protected final ExecutorService executor;

    /**
     * Initialize with codeArea.
     * @param codeArea
     */
    public Highlight(final CodeArea codeArea) {
        this.codeArea = codeArea;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Do highlighting.
     */
    public final Subscription highlight() {
        final String code = codeArea.getText();
        final Subscription subscription = codeArea.richChanges()
            .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
            .successionEnds(Duration.ofMillis(500))
            .supplyTask(this::computeHighlightingAsync)
            .awaitLatest(codeArea.richChanges())
            .filterMap(t -> {
                if(t.isSuccess()) {
                    return Optional.of(t.get());
                }
                t.getFailure().printStackTrace();
                return Optional.empty();
            })
            .subscribe(this::applyHighlighting);
        codeArea.replaceText(code);
        return subscription;
    }

    /**
     * Compute highlighting.
     * @return
     */
    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {

        final String text = codeArea.getText();

        final Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };

        if (!ShareData.isScrolling)
        {
            executor.execute(task);
        }

        return task;
    }

    /**
     * Apply highlighting.
     * @param highlighting
     */
    private void applyHighlighting(final StyleSpans<Collection<String>> highlighting) {
        //System.out.println("highlight");
        codeArea.setStyleSpans(0, highlighting);

        // Show incorrect spells
        for(FormatCode.MissInfo missInfo: ShareData.missInfoList)
        {
            StyleSpansBuilder<Collection<String>> spansBuilder
                    = new StyleSpansBuilder<>();
            spansBuilder.add(Collections.singleton("spell-error"), missInfo.missWord.length());
            codeArea.setStyleSpans(missInfo.paraNo, missInfo.startNo, spansBuilder.create());
        }

        MainApplication.showMatchingWords();
    }

    /**
     * Compute highlighting.
     * @param text
     * @return
     */
    protected abstract StyleSpans<Collection<String>> computeHighlighting(final String text);

}