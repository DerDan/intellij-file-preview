package net.seesharpsoft.intellij.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class EditorSnapshotFactory {

    private static final Map<Class, Class<? extends EditorSnapshot>> SNAPSHOT_PROVIDER;

    static {
        SNAPSHOT_PROVIDER = new HashMap<>();
        SNAPSHOT_PROVIDER.put(TextEditor.class, TextEditorSnapshot.class);
        SNAPSHOT_PROVIDER.put(TextEditorWithPreview.class, TextEditorWithPreviewSnapshot.class);
    }

    private static final EditorSnapshotFactory INSTANCE = new EditorSnapshotFactory();

    public static EditorSnapshotFactory getInstance() {
        return INSTANCE;
    }

    private EditorSnapshotFactory() {
        // Utility
    }

    public Map.Entry<Class, Class<? extends EditorSnapshot>> findSnapshotProvider(@NotNull FileEditor fileEditor) {
        Class targetClass = fileEditor.getClass();
        return SNAPSHOT_PROVIDER.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(targetClass))
                .findFirst().orElse(null);
    }

    public EditorSnapshot create(FileEditor fileEditor) {
        if (fileEditor == null) {
            return null;
        }

        Map.Entry<Class, Class<? extends EditorSnapshot>> snapshotProvider = findSnapshotProvider(fileEditor);
        if (snapshotProvider == null) {
            return null;
        }

        try {
            return snapshotProvider.getValue().getDeclaredConstructor(snapshotProvider.getKey()).newInstance(fileEditor);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException exc) {
            throw new RuntimeException(exc);
        }
    }
}
