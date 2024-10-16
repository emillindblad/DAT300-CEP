package dat300;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogLineTest {
    @Test
    void getIp() {
        String[] inputs = {
                "Mar 08 13:32:48 athena sshd[70335]: Invalid user admin from 43.135.173.175 port 35146",
                "Mar 08 13:32:48 athena sshd[70335]: Received disconnect from 43.135.173.175 port 35146:11: Bye Bye [preauth]",
                "Mar 08 13:32:48 athena sshd[70335]: Disconnected from invalid user admin 43.135.173.175 port 35146 [preauth]",
                "Mar 08 13:32:55 athena sshd[70343]: Invalid user pierre from 47.96.136.94 port 34498",
                "Mar 08 13:32:55 athena sshd[70343]: Received disconnect from 47.96.136.94 port 34498:11: Bye Bye [preauth]",
                "Mar 08 13:32:55 athena sshd[70343]: Disconnected from invalid user pierre 47.96.136.94 port 34498 [preauth]",
                "Mar 08 13:32:57 athena sshd[70345]: Invalid user eagle from 43.159.56.117 port 52922",
                "Mar 08 13:32:58 athena sshd[70345]: Received disconnect from 43.159.56.117 port 52922:11: Bye Bye [preauth]",
                "Mar 08 13:32:58 athena sshd[70345]: Disconnected from invalid user eagle 43.159.56.117 port 52922 [preauth]",
                "Mar 08 13:33:05 athena sshd[70352]: Invalid user app from 197.5.145.73 port 39523",
                "Aug 19 16:34:50 athena sshd[1524504]: Accepted publickey for mmmmmm from 129.16.0.30 port 46000 ssh2: ED25519 SHA256:hBlV5kuakAsafbO9+wQ0rBXSn4qMDdn2jIz6IdoL4nA",
                "Aug 19 16:34:50 athena sshd[1524504]: pam_systemd(sshd:session): New sd-bus connection (system-bus-pam-systemd-1524504) opened.",
                "Aug 19 16:34:51 athena sshd[1524504]: pam_unix(sshd:session): session opened for user mmmmmm(uid=1001) by mmmmmm(uid=0)",
        };

        for (String line: inputs) {
            LogLine logLine = new LogLine(line);
            System.out.println(logLine.getIp());
        };

    }

}