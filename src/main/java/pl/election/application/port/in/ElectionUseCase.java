package pl.election.application.port.in;

import pl.election.domain.model.Election;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.VotingOption;

import java.util.List;

public interface ElectionUseCase {

    Election createElection(String name);

    VotingOption addVotingOption(ElectionId electionId, String optionName);

    Election getElection(ElectionId electionId);

    List<Election> getAllElections();
}
