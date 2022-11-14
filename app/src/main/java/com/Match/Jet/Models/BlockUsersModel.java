package com.Match.Jet.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockUsersModel{
 @JsonProperty("BlockUser")
 public BlockUser blockUser;
 @JsonProperty("BlockedUser")
 public BlockedUser blockedUser;
}
