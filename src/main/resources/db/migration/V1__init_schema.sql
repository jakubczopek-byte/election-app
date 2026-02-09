CREATE TABLE voters (
    id         UUID PRIMARY KEY,
    name       VARCHAR(100)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    status     VARCHAR(20)         NOT NULL,
    created_at TIMESTAMP           NOT NULL
);

CREATE TABLE elections (
    id         UUID PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    created_at TIMESTAMP    NOT NULL
);

CREATE TABLE voting_options (
    id          UUID PRIMARY KEY,
    election_id UUID         NOT NULL REFERENCES elections (id),
    name        VARCHAR(200) NOT NULL
);

CREATE TABLE votes (
    id               UUID PRIMARY KEY,
    voter_id         UUID      NOT NULL REFERENCES voters (id),
    election_id      UUID      NOT NULL REFERENCES elections (id),
    voting_option_id UUID      NOT NULL REFERENCES voting_options (id),
    cast_at          TIMESTAMP NOT NULL,
    UNIQUE (voter_id, election_id)
);

CREATE INDEX idx_voting_options_election_id ON voting_options (election_id);
CREATE INDEX idx_votes_election_id ON votes (election_id);
CREATE INDEX idx_votes_voter_id ON votes (voter_id);
CREATE INDEX idx_votes_voting_option_id ON votes (voting_option_id);
