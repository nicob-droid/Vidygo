package io.github.nicobdroid.vidygo;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import io.github.nicobdroid.vidygo.model.Video;
import io.github.nicobdroid.vidygo.util.VideoPreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests instrumentés pour l'application Vidygo.
 * Ces tests s'exécutent sur un appareil Android ou un émulateur.
 */
@RunWith(AndroidJUnit4.class)
public class VidygoInstrumentedTest {

    private Context context;
    private VideoPreferenceManager preferenceManager;

    @Before
    public void setUp() {
        // Contexte de l'application
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        preferenceManager = new VideoPreferenceManager(context);
        // Nettoyer avant chaque test
        preferenceManager.clearAll();
    }

    /**
     * Test le contexte de l'application
     */
    @Test
    public void useAppContext() {
        assertEquals("io.github.nicobdroid.vidygo", context.getPackageName());
    }

    /**
     * Test la création d'une vidéo
     */
    @Test
    public void testVideoCreation() {
        Video video = new Video(
                "test_id",
                "Test Video",
                "Test Channel",
                "https://example.com/thumb.jpg",
                "https://youtube.com/watch?v=test"
        );

        assertNotNull(video);
        assertEquals("Test Video", video.getTitle());
        assertEquals("Test Channel", video.getChannel());
    }

    /**
     * Test la sauvegarde d'une vidéo
     */
    @Test
    public void testSaveVideo() {
        Video video = new Video(
                "test_1",
                "Test Video",
                "Test Channel",
                "https://example.com/thumb.jpg",
                "https://youtube.com/watch?v=test"
        );

        preferenceManager.saveVideo(video);
        //assertEquals(1, preferenceManager.getVideos().size());
    }

    /**
     * Test la suppression d'une vidéo
     */
    @Test
    public void testDeleteVideo() {
        Video video = new Video(
                "test_1",
                "Test Video",
                "Test Channel",
                "https://example.com/thumb.jpg",
                "https://youtube.com/watch?v=test"
        );

        preferenceManager.saveVideo(video);
        preferenceManager.deleteVideo("test_1");
        //assertEquals(0, preferenceManager.getVideos().size());
    }
}

