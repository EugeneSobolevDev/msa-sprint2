import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import gql from 'graphql-tag';

const typeDefs = gql`
  extend schema
    @link(
      url: "https://specs.apollo.dev/federation/v2.0",
      import: ["@key", "@override", "@external", "@requires"]
    )

  extend type Booking @key(fields: "id") {
    id: ID! @external
    userId: ID! @external
    promoCode: String @external
    originalDiscountPercent: Float @external
    discountPercent: Float
      @override(from: "booking-subgraph")
      @requires(fields: "promoCode userId")
    discountInfo: DiscountInfo @requires(fields: "originalDiscountPercent")
  }

  type DiscountInfo {
    isValid: Boolean
    originalDiscount: Float
    finalDiscount: Float
    description: String
  }
`;

// Mock Promo DB
const promoDB = {
  'TESTCODE1': { isValid: true, discount: 25, description: 'Тестовый промокод: 25%' },
  'PROMO20': { isValid: true, discount: 20, description: 'Тестовый промокод: 20%' },
  'INVALID': { isValid: false, discount: 0, description: 'Промокод недействителен' },
};

const resolvers = {
  Booking: {
    discountInfo: (booking, _, context) => {
      console.log('=== promocode: discountInfo resolver ===');
      const promo = promoDB[booking.promoCode];

      return {
        isValid: promo?.isValid ?? false,
        originalDiscount: booking.originalDiscountPercent ?? 0,
        finalDiscount: promo?.isValid ? promo.discount : booking.originalDiscountPercent ?? 0,
        description: promo?.description ?? 'Промокод отсутствует',
      };
    },

    discountPercent: (booking, _, context) => {
      const promo = promoDB[booking.promoCode];
      return promo?.isValid
      ? promo.discount
      : booking.originalDiscountPercent ?? 0;
    },
  },
};

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
  csrfPrevention: false,
});

startStandaloneServer(server, {
  listen: { port: 4003 },
}).then(() => {
  console.log('✅ promo-subgraph http://localhost:4003');
});