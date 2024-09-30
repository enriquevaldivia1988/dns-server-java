[![progress-banner](https://backend.codecrafters.io/progress/dns-server/0ab8ba26-34bb-400e-a8f1-23f80fc1e1e7)](https://app.codecrafters.io/users/codecrafters-bot?r=2qF)

This is a starting point for Java solutions to the
["Build Your Own DNS server" Challenge](https://app.codecrafters.io/courses/dns-server/overview).

## Challenge Overview

In this challenge, you'll build a DNS server capable of:
- Parsing and creating DNS packets
- Responding to DNS queries
- Handling various record types (A, AAAA, CNAME, etc.)
- Performing recursive resolve

Along the way, you'll learn about:
- DNS protocol
- DNS packet format
- Root servers
- Authoritative servers
- Forwarding servers
- 
**Note**: If you're viewing this repo on GitHub, head over to
[codecrafters.io](https://codecrafters.io) to try the challenge.

# Passing the first stage

The entry point for your `your_program.sh` implementation is in
`src/main/java/Main.java`. Study and uncomment the relevant code, and push your
changes to pass the first stage:

```sh
git commit -am "pass 1st stage" # any msg
git push origin master
```

Time to move on to the next stage!

# Stage 2 & beyond

Note: This section is for stages 2 and beyond.

1. Ensure you have `java (21)` installed locally
1. Run `./your_program.sh` to run your program, which is implemented in
   `src/main/java/Main.java`.
1. Commit your changes and run `git push origin master` to submit your solution
   to CodeCrafters. Test output will be streamed to your terminal.
