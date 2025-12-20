import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import gql from 'graphql-tag';
import DataLoader from 'dataloader';

const typeDefs = gql`
  extend schema
    @link(
      url: "https://specs.apollo.dev/federation/v2.0"
      import: ["@key", "@shareable", "@override", "@external", "@provides", "@requires"]
    )

  type Hotel @key(fields: "id") {
    id: ID!
    name: String
    city: String
    stars: Int
  }

  type Query {
    hotelsByIds(ids: [ID!]!): [Hotel]
  }
`;

const createHotelDataLoader = () => {
  return new DataLoader(async (hotelIds) => {
    console.log(`Batch loading ${hotelIds.length} hotels:`, hotelIds);

    const uniqueIds = [...new Set(hotelIds.filter(id => id != null))];

    if (uniqueIds.length === 0) {
      return hotelIds.map(() => null);
    }

    try {
      let hotels = await Promise.all(
        uniqueIds.map(async (id) => {
          try {
            const reqUrl = `http://host.docker.internal:8084/api/hotels/${id}`;
            const response = await fetch(reqUrl);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
          } catch (err) {
            console.error(`Failed to load hotel ${id}:`, err.message);
            return null;
          }
        })
      );

      const hotelMap = new Map();
      hotels.forEach(hotel => {
        if (hotel && hotel.id) {
          hotelMap.set(String(hotel.id), {
            id: hotel.id,
            name: null,
            city: hotel.city,
            stars: Number.parseInt(hotel.rating) || 0,
          });
        }
      });

      return hotelIds.map(id => {
        if (id == null) return null;
        return hotelMap.get(String(id)) || null;
      });
    } catch (err) {
      console.error(`Failed to load hotels:`, err);
      return hotelIds.map(() => null);
    }
  }, {
    cache: true,
    cacheKeyFn: (key) => String(key),
    maxBatchSize: 100,
    batchScheduleFn: (callback) => setTimeout(callback, 10),
  });
};

const hotelDataLoader = createHotelDataLoader();

const resolvers = {
  Hotel: {
    __resolveReference: async ({ id }) => {
      if (!id) return null;
      return await hotelDataLoader.load(id);
    },
  },
  Query: {
    hotelsByIds: async (_, { ids }) => {
      if (!ids || ids.length === 0) return [];
      const results = await hotelDataLoader.loadMany(ids);
      return results.filter(hotel => hotel != null);
    },
  },
};

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
  csrfPrevention: false,
  context: async () => ({
    hotelDataLoader: createHotelDataLoader(),
   }),
});

startStandaloneServer(server, {
  listen: { port: 4002 },
}).then(() => {
  console.log('✅ Hotel subgraph ready at http://localhost:4002/');
});