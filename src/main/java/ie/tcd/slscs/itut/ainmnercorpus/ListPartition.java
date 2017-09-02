/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Jim O'Regan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ie.tcd.slscs.itut.ainmnercorpus;

import opennlp.tools.util.Span;

import java.util.ArrayList;
import java.util.List;

public class ListPartition {
    public static String makeText(EntityBase[] paragraph, Span[] sentences, Span[] tokens) throws Exception {
        StringBuilder sb = new StringBuilder();

        for(Span sent : split_sentences(sentences, paragraph)) {
            List<EntityBase> ents = split_entities(sentences, paragraph);
            Span entspans[] = entityToSpan(ents);
            for(int i = 0; i < ents.size(); i++) {
                sb.append(ents.get(i).beforeText());
                List<Span> toks = partition(tokens, entspans[i]);
                for(int j = 0; j < toks.size(); j++) {
                    sb.append(ents.get(i).getText().substring(toks.get(j).getStart(), toks.get(j).getEnd()));
                    if(j < toks.size() - 1) {
                        sb.append(" ");
                    }
                }
                sb.append(ents.get(i).afterText());
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    static List<Span> partition(Span[] tokens, Span entity) {
        List<Span> part = new ArrayList<>();
        for(Span span : tokens) {
            if(span.getStart() >= entity.getStart() && span.getEnd() <= entity.getEnd()) {
                part.add(span);
            }
        }
        return part;
    }
    static List<Span> split_sentences(Span[] sentences, EntityBase[] entities) throws Exception {
        List<Span> out = new ArrayList<>();
        Span[] entity_spans = entityToSpan(entities);

        for(int i = 0; i < sentences.length; i++) {
            Span current_sentence = sentences[i];
            for(int j = 0; j < entity_spans.length; j++) {
                Span current_entity_span = entity_spans[j];
                if(current_entity_span.getEnd() > current_sentence.getEnd()
                        && current_entity_span.getStart() < current_sentence.getEnd()) {
                    // Sentence can't end within an entity, so rewrite the span
                    if(entities[j] instanceof SimpleEntity) {
                        if (i > sentences.length - 1) {
                            throw new Exception("More content expected");
                        }
                        i++;
                        Span next_sentence = sentences[i];
                        current_sentence = new Span(current_sentence.getStart(), next_sentence.getEnd());
                    }
                }
            }
            out.add(current_sentence);
        }
        return out;
    }
    static List<EntityBase> split_entities(Span[] sentences, EntityBase[] entities) throws Exception {
        List<EntityBase> out = new ArrayList<>();
        Span[] entity_spans = entityToSpan(entities);
        for(int i = 0; i < sentences.length; i++) {
            for(int j = 0; j < entity_spans.length; j++) {
                if(entity_spans[j].getStart() < sentences[i].getStart()) {
                    continue;
                }
                if(i < sentences.length && sentences[i].getEnd() >= entity_spans[j].getEnd()) {
                    out.add(entities[j]);
                } else {
                    while(i < sentences.length && entity_spans[j].getEnd() >= sentences[i].getEnd()) {
                        if(!(entities[j] instanceof TextEntity)) {
                            throw new Exception("Unexpected entity: use split_sentences first");
                        }
                        out.add(new TextEntity(entities[j].getText().substring(sentences[i].getStart(), sentences[i].getEnd())));
                        i++;
                    }
                    if(i < sentences.length && entity_spans[j].getEnd() <= sentences[i].getEnd()) {
                        out.add(new TextEntity(entities[j].getText().substring(sentences[i].getStart(), entity_spans[j].getEnd())));
                    }
                }
            }
        }
        return out;
    }

    static Span[] entityToSpan(EntityBase[] paragraph) {
        Span entity_spans[] = new Span[paragraph.length];

        int current_pos = 0;
        for(int i = 0; i < paragraph.length; i++) {
            int item_length = paragraph[i].getText().length();
            entity_spans[i] = new Span(current_pos, current_pos + item_length);
            current_pos += item_length;
        }

        return entity_spans;
    }

    static Span[] entityToSpan(List<EntityBase> paragraph) {
        Span entity_spans[] = new Span[paragraph.size()];

        int current_pos = 0;
        for(int i = 0; i < paragraph.size(); i++) {
            int item_length = paragraph.get(i).getText().length();
            entity_spans[i] = new Span(current_pos, current_pos + item_length);
            current_pos += item_length;
        }

        return entity_spans;
    }

    public static String getEntityBaseString(EntityBase[] eb) {
        StringBuilder sb = new StringBuilder();
        for(EntityBase e : eb) {
            sb.append(e.getText());
        }
        return sb.toString();
    }
}
