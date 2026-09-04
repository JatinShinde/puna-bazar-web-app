package com.punabazar.util;

import com.jcraft.jsch.*;
import java.io.File;

public class Deployer {
    public static void main(String[] args) {
        String host = "187.127.188.55";
        int[] ports = {65002, 22, 2222, 22022};
        String user = "root";
        String pass = "SpdpBoss@2026#";
        String localFile = "target/puna-bazar-app-1.0.0.jar";
        String remoteDir = "/opt/punabazar";

        int workingPort = -1;
        for (int p : ports) {
            try (java.net.Socket socket = new java.net.Socket()) {
                System.out.println("🔍 Testing connection to " + host + ":" + p + "...");
                socket.connect(new java.net.InetSocketAddress(host, p), 3000);
                System.out.println("✅ Found open SSH port: " + p);
                workingPort = p;
                break;
            } catch (Exception e) {
                System.out.println("❌ Port " + p + " closed or filtered.");
            }
        }

        if (workingPort == -1) {
            System.err.println("⚠️ Could not reach SSH on ports 65002, 22, 2222. Please check Hostinger SSH Access in hPanel.");
            return;
        }

        int port = workingPort;
        System.out.println("🚀 Connecting to Hostinger VPS (" + host + ":" + port + ")...");
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftp = null;
        try {
            session = jsch.getSession(user, host, port);
            session.setPassword(pass);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(15000);
            System.out.println("✅ Connected to Hostinger VPS!");

            Channel channel = session.openChannel("sftp");
            channel.connect(10000);
            sftp = (ChannelSftp) channel;

            File jarFile = new File(localFile);
            System.out.println("📦 Uploading updated " + jarFile.getName() + " (" + (jarFile.length() / 1024) + " KB) to " + remoteDir + "...");

            try {
                sftp.mkdir(remoteDir);
            } catch (Exception ignored) {}
            sftp.cd(remoteDir);
            sftp.put(localFile, "puna-bazar-app-1.0.0.jar");
            System.out.println("🎉 File Upload Complete!");

            sftp.disconnect();

            // Restart live application on Hostinger VPS
            System.out.println("⚡ Restarting Live Application on Hostinger VPS...");
            ChannelExec exec = (ChannelExec) session.openChannel("exec");
            exec.setCommand("pkill -9 -f 'puna-bazar-app' || true; cd /opt/punabazar && nohup java -jar puna-bazar-app-1.0.0.jar > app.log 2>&1 &");
            exec.connect(10000);
            Thread.sleep(2000);
            exec.disconnect();

            System.out.println("🌟 LIVE DEPLOYMENT COMPLETE! App restarted successfully on http://187.127.188.55:8086");

        } catch (Exception e) {
            System.err.println("❌ Deployment Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
