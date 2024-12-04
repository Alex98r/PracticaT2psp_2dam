package es.studium.Practica_T2_PSP;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ventanaAplicacion extends JFrame {
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private ArrayList<File> musicFiles;
    private List<MusicPlayer> musicPlayers;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ventanaAplicacion app = new ventanaAplicacion();
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ventanaAplicacion() {
        setTitle("Music Player");
        setSize(600, 400); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        add(new JScrollPane(fileList), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton playButton = new JButton("Reproducir");
        JButton stopButton = new JButton("Parar");
        controlPanel.add(playButton);
        controlPanel.add(stopButton);
        add(controlPanel, BorderLayout.SOUTH);

        playButton.addActionListener(e -> playSelectedFile());
        stopButton.addActionListener(e -> stopSelectedFile());

        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    playSelectedFile();
                }
            }
        });

        musicPlayers = new ArrayList<>();
        searchMusicFiles();
    }

    private void searchMusicFiles() {
        musicFiles = new ArrayList<>();
        ArrayList<String> extensions = new ArrayList<>();
        extensions.add(".mp3");
        extensions.add(".wav");

        File[] roots = File.listRoots();
        for (File root : roots) {
            searchFiles(root, extensions);
        }

        for (File file : musicFiles) {
            listModel.addElement(file.getAbsolutePath());
        }
    }

    private void searchFiles(File dir, ArrayList<String> extensions) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    searchFiles(file, extensions);
                } else {
                    for (String ext : extensions) {
                        if (file.getName().toLowerCase().endsWith(ext)) {
                            musicFiles.add(file);
                        }
                    }
                }
            }
        }
    }

    private void playSelectedFile() {
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex != -1) {
            File selectedFile = musicFiles.get(selectedIndex);
            MusicPlayer player = new MusicPlayer();
            musicPlayers.add(player);
            player.play(selectedFile);
        } else {
            showWarningDialog("Por favor, selecciona un archivo para reproducir.");
        }
    }

    private void stopSelectedFile() {
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex != -1) {
            File selectedFile = musicFiles.get(selectedIndex);
            for (MusicPlayer player : musicPlayers) {
                if (player.isPlaying(selectedFile)) {
                    player.stop();
                    musicPlayers.remove(player);
                    break;
                }
            }
        } else {
            showWarningDialog("Por favor, selecciona un archivo para parar.");
        }
    }

    private void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    class MusicPlayer {
        private Player mp3Player;
        private Clip wavClip;
        private Thread playerThread;
        private File currentFile;

        public void play(File file) {
            currentFile = file;
            if (file.getName().toLowerCase().endsWith(".mp3")) {
                playMp3(file);
            } else if (file.getName().toLowerCase().endsWith(".wav")) {
                playWav(file);
            }
        }

        private void playMp3(File file) {
            try {
                FileInputStream fis = new FileInputStream(file);
                mp3Player = new Player(fis);
                playerThread = new Thread(() -> {
                    try {
                        Thread.sleep(5000); // Añadir retraso de 5 segundos
                        mp3Player.play();
                    } catch (JavaLayerException | InterruptedException e) {
                        e.printStackTrace();
                        showErrorDialog("Error al reproducir el archivo: " + file.getAbsolutePath());
                    }
                });
                playerThread.start();
            } catch (FileNotFoundException | JavaLayerException e) {
                e.printStackTrace();
                showErrorDialog("Error al reproducir el archivo: " + file.getAbsolutePath());
            }
        }

        private void playWav(File file) {
            playerThread = new Thread(() -> {
                try {
                    Thread.sleep(5000); // Añadir retraso de 5 segundos
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                    wavClip = AudioSystem.getClip();
                    wavClip.open(audioStream);
                    wavClip.start();
                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException | InterruptedException e) {
                    e.printStackTrace();
                    showErrorDialog("Error al reproducir el archivo: " + file.getAbsolutePath());
                }
            });
            playerThread.start();
        }

        public void stop() {
            if (mp3Player != null) {
                mp3Player.close();
                playerThread.interrupt();
            }
            if (wavClip != null && wavClip.isRunning()) {
                wavClip.stop();
                wavClip.close();
            }
        }

        public boolean isPlaying(File file) {
            return currentFile != null && currentFile.equals(file);
        }

        private void showErrorDialog(String message) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ventanaAplicacion.this, message, "Error", JOptionPane.ERROR_MESSAGE));
        }
    }
}
