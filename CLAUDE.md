# Escape the Brainrot: Reclaim Your Mind (project brief for AI assistants)

## What this is
A Java 2D retro horror adventure game for an OOP 101 final project, built by a team of 3 students who are learning game development for the first time. Players defeat Italian brainrot meme bosses by solving Java OOP code challenges, lowering a Brainrot Gauge to zero.

## Hard constraints
- Java 21, Swing and AWT only. No third-party libraries, no JavaFX, no Gradle or Maven dependencies.
- Images: javax.imageio. Sound: javax.sound.sampled (WAV only). Saves: java.util.Properties.
- Load resources with getResourceAsStream from the res/ folder.
- Must demonstrate encapsulation, inheritance, polymorphism, abstraction, exception handling, and arrays/collections naturally, never forced.
- The project folder name may contain spaces. Always quote paths in scripts.

## Architecture (do not change without asking)
- Packages: engine, world, entity, battle, challenge, systems, exception, ui.
- Game loop at 60 updates per second in GamePanel; screens extend abstract GameState and are switched by StateManager.
- Screens: TitleState and OverworldState live in engine/, BattleState lives in battle/.
- Tile-locked movement: logic in tile coordinates, drawing in pixel coordinates.
- Gauge = Rot + Strain. Rot is locked per undefeated boss and only drops when that boss is defeated. Strain rises with time, wrong answers, and AI use, and falls with correct answers. Starts at Rot 40, Strain 50. Fried at 100.
- All balance numbers live in res/config.properties, never hard-coded.
- Challenges are loaded from text files in res/challenges/.

## Ownership
- Zrs: engine/, world/, entity/, ui/, Main.java, project setup files.
- Andrew: battle/, challenge/, res/challenges/.
- Aeron: systems/, exception/, docs/, README.md, screenshots/.
- Do not edit files outside the current person's ownership unless they ask.

## How to help me
- I am a student. Explain before you write code. Prefer small steps I can follow.
- When I ask for code, give the smallest piece that solves my current step, with a short explanation of each part.
- Do not rewrite files I did not ask about. Do not rename classes or change the architecture.
- If my request breaks a constraint above, tell me instead of doing it.
- When writing a "what does this print" challenge, remind me to run the code to confirm the answer.
- Keep code readable: clear names, small methods, short comments where the logic is not obvious.

## My role
I am Zrs. I own engine/, world/, entity/, ui/, and Main.java, and I am building the base scaffold.