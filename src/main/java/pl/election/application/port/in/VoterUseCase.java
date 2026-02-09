package pl.election.application.port.in;

import pl.election.domain.model.Voter;
import pl.election.domain.model.VoterId;

import java.util.List;

public interface VoterUseCase {

    Voter createVoter(String name, String email);

    Voter blockVoter(VoterId id);

    Voter unblockVoter(VoterId id);

    Voter getVoter(VoterId id);

    List<Voter> getAllVoters();
}
