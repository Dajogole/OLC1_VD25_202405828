package ui;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class EditorTabPanel extends JPanel {

    private final JTabbedPane tabbedPane;
    private final Map<Component, EditorTabInfo> tabsInfo;
    private int untitledCounter = 1;

    private static class EditorTabInfo {
        Path filePath;
        JTextArea textArea;

        EditorTabInfo(Path filePath, JTextArea textArea) {
            this.filePath = filePath;
            this.textArea = textArea;
        }
    }

    public EditorTabPanel() {
        setLayout(new BorderLayout());
        this.tabbedPane = new JTabbedPane();
        this.tabsInfo = new HashMap<Component, EditorTabInfo>();
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void newUntitledFile() {
        String title = "Sin título " + untitledCounter++;
        createTab(null, title, "");
    }

    public void openFile(Path filePath, String content) {
        String title = filePath.getFileName().toString();
        createTab(filePath, title, content);
    }

    private void createTab(Path filePath, String title, String content) {
        JTextArea textArea = new JTextArea();
        textArea.setText(content);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setTabSize(4);

        JScrollPane scrollPane = new JScrollPane(textArea);

        EditorTabInfo info = new EditorTabInfo(filePath, textArea);
        tabsInfo.put(scrollPane, info);

        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);
    }

    public boolean hasOpenTabs() {
        return tabbedPane.getTabCount() > 0;
    }

    public JTextArea getCurrentTextArea() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp == null) {
            return null;
        }
        EditorTabInfo info = tabsInfo.get(comp);
        return info != null ? info.textArea : null;
    }

    public Path getCurrentFilePath() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp == null) {
            return null;
        }
        EditorTabInfo info = tabsInfo.get(comp);
        return info != null ? info.filePath : null;
    }

    public void setCurrentFilePath(Path newPath) {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp == null) {
            return;
        }
        EditorTabInfo info = tabsInfo.get(comp);
        if (info != null) {
            info.filePath = newPath;
            String title = newPath.getFileName().toString();
            int index = tabbedPane.indexOfComponent(comp);
            if (index >= 0) {
                tabbedPane.setTitleAt(index, title);
            }
        }
    }

    public String getCurrentFileContent() {
        JTextArea area = getCurrentTextArea();
        return area != null ? area.getText() : null;
    }

    public void setCurrentFileContent(String text) {
        JTextArea area = getCurrentTextArea();
        if (area != null) {
            area.setText(text);
        }
    }

    public void closeCurrentTab() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp == null) {
            return;
        }
        tabsInfo.remove(comp);
        int index = tabbedPane.indexOfComponent(comp);
        if (index >= 0) {
            tabbedPane.removeTabAt(index);
        }
    }

    public String getCurrentTabTitle() {
        int index = tabbedPane.getSelectedIndex();
        if (index < 0) {
            return null;
        }
        return tabbedPane.getTitleAt(index);
    }
}
