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
    }

    @Test
    public void split_entities() throws Exception {
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