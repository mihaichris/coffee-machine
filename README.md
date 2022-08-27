# coffee-machine

## Description

This is a coffee machine built in Scala using actors. Logically, an actor is a concurrent process which executes the body of its act method, and then terminates. All the actors are supervised by the `CoffeMachine` process. All actors work asyncronously to make different types of coffee or tea.
The actors available in the coffee machine are: `BrewActor`, `CappuccinoActor`, `CombineActor`, `FrothMilkActor`, `GrassActor`, `GrindActor`, `HeatWaterActor`, `TeaActor`, `UserInteractionActor`, `WaterStorageActor`.

## Basic Usage
Run the app and write what is prompted in the CLI.