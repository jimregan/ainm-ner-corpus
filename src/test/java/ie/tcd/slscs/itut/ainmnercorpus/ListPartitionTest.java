package ie.tcd.slscs.itut.ainmnercorpus;

import opennlp.tools.util.Span;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ListPartitionTest {
    @Test
    public void partition() throws Exception {
    }

    @Test
    public void split_sentences() throws Exception {
        TextEntity txt1 = new TextEntity("There was a house in ");
        SimpleEntity se1 = new SimpleEntity("Co. Clare", "place");
        TextEntity txt2 = new TextEntity(". Many years ago.");
        EntityBase arr[] = new EntityBase[]{txt1, se1, txt2};
        Span sent[] = new Span[]{new Span(0, 24), new Span(25, 31), new Span(32, 45)};
        Span exp[] = new Span[]{new Span(0, 24), new Span(32, 45)};
        List<Span> out = ListPartition.split_sentences(sent, arr);
        assertEquals(2, out.size());
        assertEquals(31, out.get(0).getEnd());
        assertEquals(32, out.get(1).getStart());
    }

    @Test
    public void makeText() throws Exception {
        TextEntity txt = new TextEntity("A piece of text. With a sentence. Or three. In ");
        SimpleEntity se1 = new SimpleEntity("Co. Cork", "place");
        TextEntity txt2 = new TextEntity(" of all places.");
        EntityBase ents[] = new EntityBase[]{txt, se1, txt2};
        Span sents[] = new Span[]{new Span(0, 16), new Span(17, 33), new Span(34, 43), new Span(44, 70)};
        Span toks[] = new Span[]{
                new Span(0, 1), new Span(2, 7), new Span(8, 10), new Span(11, 15),
                new Span(15, 16), new Span(17, 21), new Span(22, 23), new Span(24, 32),
                new Span(32, 33), new Span(34, 36), new Span(37, 42), new Span(42, 43),
                new Span(44, 46), new Span(47, 49), new Span(49, 50), new Span(51, 55),
                new Span(56, 58), new Span(59, 62), new Span(63, 69), new Span(69, 70),
            };
        String exp = "A piece of text .\nWith a sentence .\nOr three .\nIn <START:place> Co . Cork <END> of all places .\n";
        String out = ListPartition.makeText(ents, sents, toks);
        assertEquals(exp, out);
    }

    @Test
    public void split_entities() throws Exception {
        TextEntity txt = new TextEntity("A piece of text. With a sentence. Or three.");
        TextEntity text[] = new TextEntity[]{txt};
        Span sents[] = new Span[]{new Span(0, 16), new Span(17, 33), new Span(34, 43)};
        List<EntityBase> out = ListPartition.split_entities(sents, text);
        assertEquals(3, out.size());
        assertEquals("A piece of text.", out.get(0).getText());
        assertEquals("Or three.", out.get(2).getText());
    }

    @Test
    public void split_entities2() throws Exception {
        TextEntity txt = new TextEntity("A piece of text. With a sentence. Or three. In ");
        SimpleEntity se1 = new SimpleEntity("Co. Cork", "place");
        TextEntity txt2 = new TextEntity(" of all places.");
        EntityBase ents[] = new EntityBase[]{txt, se1, txt2};
        Span sents[] = new Span[]{new Span(0, 16), new Span(17, 33), new Span(34, 43), new Span(44, 70)};
        List<EntityBase> out = ListPartition.split_entities(sents, ents);
        assertEquals("A piece of text.", out.get(0).getText());
        assertEquals("Or three.", out.get(2).getText());
        assertEquals("In ", out.get(3).getText());
        assertEquals(6, out.size());
    }

    @Test
    public void entityToSpan() throws Exception {
        SimpleEntity pers = new SimpleEntity("John", "person");
        TextEntity txt = new TextEntity(" walked in");
        EntityBase arr[] = new EntityBase[]{pers, txt};
        Span exp[] = new Span[]{new Span(0, 4), new Span(4, 14)};
        Span out[] = ListPartition.entityToSpan(arr);
        assertArrayEquals(exp, out);
    }

    @Test
    public void getEntityBaseString() throws Exception {
        SimpleEntity pers = new SimpleEntity("John", "person");
        TextEntity txt = new TextEntity(" walked in");
        EntityBase arr[] = new EntityBase[]{pers, txt};
        String exp = "John walked in";
        String out = ListPartition.getEntityBaseString(arr);
        assertEquals(exp, out);
    }

}