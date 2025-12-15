import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import gql from 'graphql-tag';
import { GraphQLError } from 'graphql';

const typeDefs = gql`
  extend schema
    @link(
      url: "https://specs.apollo.dev/federation/v2.0"
      import: ["@key", "@external"]
    )

  type Booking @key(fields: "id") {
    id: ID!
    userId: ID!
    hotelId: ID!
    promoCode: String
    discountPercent: Float
  }

  type Query {
    bookingsByUser(userId: ID!): [Booking]
  }

  extend type Hotel @key(fields: "id") {
    id: ID! @external
  }

  extend type Booking @key(fields: "id") {
    hotel: Hotel!
    originalDiscountPercent: Float
  }
`;

async function getBookingsByUserId(userId) {
    const res = await fetch(
        `http://host.docker.internal:8084/api/bookings?userId=${userId}`
    );
    return res.json();
}

async function getBookingById(id) {
    const res = await fetch(
        `http://host.docker.internal:8084/api/bookings/${id}`
    );
    if (!res.ok) {
        throw new Error('Failed to fetch booking');
    }
    return res.json();
}

function requireUser(context) {
    if (!context.userId) {
        throw unauthenticated('User is not authenticated');
    }
    return context.userId;
}

function assertBookingOwner(booking, userId) {
    if (!booking || String(booking.userId) !== String(userId)) {
        throw forbidden('You do not have access to this booking');
    }
}

function forbidden(message) {
    return new GraphQLError(message, {
        extensions: { code: 'FORBIDDEN' },
    });
}

function unauthenticated(message) {
    return new GraphQLError(message, {
        extensions: { code: 'UNAUTHENTICATED' },
    });
}

const resolvers = {
    Query: {
        bookingsByUser: async (_, { userId }, context) => {
            const authUserId = requireUser(context);

            if (String(userId) !== String(authUserId)) {
                throw forbidden('You do not have access to this booking');
            }

            return getBookingsByUserId(userId);
        },
    },

    Booking: {
        __resolveReference: async ({ id }, context) => {
            const userId = requireUser(context);
            const booking = await getBookingById(id);
            assertBookingOwner(booking, userId);
            return booking;
        },

        hotel: (booking, _, context) => {
            const userId = requireUser(context);
            assertBookingOwner(booking, userId);
            return { __typename: 'Hotel', id: booking.hotelId };
        },

        originalDiscountPercent: (booking, _, context) => {
            const userId = requireUser(context);
            assertBookingOwner(booking, userId);
            return booking.discountPercent;
        },
    },
};

const server = new ApolloServer({
    schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
    csrfPrevention: false,
});

startStandaloneServer(server, {
    listen: { port: 4001 },
    context: async ({ req }) => ({
        userId: req.headers['user-id'],
    }),
}).then(() => {
    console.log('✅ booking-subgraph running at http://localhost:4001');
});
