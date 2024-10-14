# VaultProvider
Simple economy provider for [Vault](https://github.com/MilkBowl/Vault)

# Prerequisites

1. Vault: This plugin requires Vault to function. Make sure Vault is installed on your server.
2. Permissions Plugin (Optional): For managing access to commands, a permissions plugin (e.g., LuckPerms) is recommended.

# Commands

## Player commands

| Command                   | Description                                         | Permission                        |
|---------------------------|-----------------------------------------------------|-----------------------------------|
| /balance                  | Get your own balance                                | vaultprovider.balance.self        |
| /balance [player]         | Get another players balance                         | vaulprovider.balance.other        |
| /bank [bank] balance      | Get the balance of a bank you are a member of       | vaultprovider.balance.bank        |
| /deposit                  | Deposit money into a bank you are a member of       | vaultprovider.deposit             |
| /withdraw                 | Withdraw money from a bank you are a member of      | vaultprovider.withdraw            |
| /pay                      | Pay another player                                  | vaultprovider.pay                 |
| /bank [bank] getMembers   | Get a list of members of a bank you are a member of | vaultprovider.bank.members.get    |
| /bank [bank] invite       | Invite a player to a bank you own                   | vaultprovider.bank.members.invite |
| /bank [bank] revokeInvite | Revoke an invitation sent to a player               | vaultprovider.bank.members.invite |
| /bank [bank] removeMember | Remove a member from a bank you own                 | vaultprovider.bank.members.remove |
| /bank [bank] acceptInvite | Accept an invitation for a bank                     | N/A*                              |
*If you want to disable invites just dont give players the `vaultprovider.bank.members.invite` permission
## Admin commands

| Command                                    | Description                         |
|--------------------------------------------|-------------------------------------|
| /eco account [player(s)] deposit [amount]  | Add money to a players account      |
| /eco account [player(s)] withdraw [amount] | Remove money from a players account |
| /eco account [player(s)] getBal            | Get the players balance             |
| /eco account [player(s)] setBal [value]    | Set the players balance             |
| /eco bank [bank] deposit [amount]          | Add money to the bank               |
| /eco bank [bank] withdraw [amount]         | Remove money from the bank          |
| /eco bank [bank] getBal                    | Get the banks balance               |
| /eco bank [bank] setBal [value]            | Set the banks balance               |
| /eco bank [bank] getOwner                  | Get the banks owner                 |
| /eco bank [bank] setOwner [player]         | Set the banks owner                 |
| /eco bank [bank] getMembers                | Get all members of a bank           |
| /eco bank [bank] addMember [player]        | Add a member to a bank              |
| /eco bank [bank] removeMember [player]     | Remove a member from a bank         |
| /eco bank [bank] delete                    | Delete a bank                       |
| /eco createBank [name]                     | Create a bank                       |