import React, { Component } from "react";
import { Profile } from "../components/Profile";

import { Search } from "../components/Images";
import { Outlet, useParams } from "react-router-dom";
import { get_friends } from "../api/request";
import { useDispatch, useSelector } from "react-redux";
import { execute } from "../store";
import { createRef } from "react";
import { Helmet } from "react-helmet";

export const Dashboard = (props) => {
  return (
    <DashboardComponent
      {...props}
      params={useParams()}
      dispatch={useDispatch()}
      storeData={useSelector((state) => state.reducer)}
    />
  );
};

class DashboardComponent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isWaiting: true,
      filter: "",
    };

    this.filter = createRef();
  }

  async componentDidMount() {
    if (!this.props.storeData.data || this.props.storeData.data.length === 0) {
      let friends = await get_friends();

      if (friends && friends.code === 200)
        this.props.dispatch(
          execute({
            type: "update_users",
            payload: friends.results.map((elm) => {
              if (elm.id === this.props.params.id) {
                elm.count = 0;
              }
              return elm;
            }),
          })
        );
    }
    this.setState({ isWaiting: false });
  }

  setFilter() {
    this.setState({ filter: this.filter.current.value });
  }

  render() {
    const { isWaiting, filter } = this.state;
    const { data } = this.props.storeData;
    return (
      <>
        <Helmet>
          <title>Jakle - Dashboard</title>
        </Helmet>
        <section className="page-container layout-page center justify-start row-flex dashboard">
          <div className="profile-section height-container container">
            <div className="search">
              <div className="data-input">
                <span className="flag" style={{ width: "35px" }}>
                  {Search}
                </span>
                <input
                  className="data-field row"
                  ref={this.filter}
                  spellCheck={false}
                  type={"text"}
                  placeholder="Search"
                  onKeyUp={(e) => {
                    this.setFilter();
                  }}
                />
              </div>
            </div>
            {isWaiting ? (
              <>
                <Profile type={0} />
                <Profile type={0} />
                <Profile type={0} />
                <Profile type={0} />
                <Profile type={0} />
                <Profile type={0} />
                <Profile type={0} />
              </>
            ) : (
              <>
                {!data || data.length === 0 ? (
                  <>
                    <div className="section-message center">
                      You don't have any friends yet. Please add some friends in
                      the discover section.
                    </div>
                  </>
                ) : filter && filter !== "" ? (
                  <>
                    {data
                      .filter(
                        (elm) =>
                          elm.name.match(new RegExp(filter, "i")) &&
                          elm.activity &&
                          elm.count
                      )
                      .map((elm) => (
                        <Profile key={elm.id} user={elm} type={1} />
                      ))}
                    {data
                      .filter(
                        (elm) =>
                          elm.name.match(new RegExp(filter, "i")) &&
                          elm.activity &&
                          !elm.count
                      )
                      .map((elm) => (
                        <Profile key={elm.id} user={elm} type={1} />
                      ))}
                    {data
                      .filter(
                        (elm) =>
                          elm.name.match(new RegExp(filter, "i")) &&
                          !elm.activity &&
                          elm.count
                      )
                      .map((elm) => (
                        <Profile key={elm.id} user={elm} type={1} />
                      ))}
                    {data
                      .filter(
                        (elm) =>
                          elm.name.match(new RegExp(filter, "i")) &&
                          !elm.activity &&
                          !elm.count
                      )
                      .map((elm) => (
                        <Profile key={elm.id} user={elm} type={1} />
                      ))}
                  </>
                ) : (
                  <>
                    {data
                      .filter((e) => e.activity && e.count)
                      .map((elm) => (
                        <Profile key={elm.id} user={elm} type={1} />
                      ))}
                    {data
                      .filter((e) => e.activity && !e.count)
                      .map((elm) => (
                        <Profile key={elm.id} user={elm} type={1} />
                      ))}
                    {data
                      .filter((e) => !e.activity && e.count)
                      .map((elm) => (
                        <Profile key={elm.id} user={elm} type={1} />
                      ))}
                    {data
                      .filter((e) => !e.activity && !e.count)
                      .map((elm) => (
                        <Profile key={elm.id} user={elm} type={1} />
                      ))}
                  </>
                )}
              </>
            )}
          </div>
          <Outlet />
        </section>
      </>
    );
  }
}
